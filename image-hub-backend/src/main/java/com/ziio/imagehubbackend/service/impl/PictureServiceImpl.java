package com.ziio.imagehubbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziio.imagehubbackend.api.aliyunai.AliYunAiApi;
import com.ziio.imagehubbackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.ziio.imagehubbackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.ziio.imagehubbackend.common.ResultUtil;
import com.ziio.imagehubbackend.constant.UserConstant;
import com.ziio.imagehubbackend.entity.Picture;
import com.ziio.imagehubbackend.entity.Space;
import com.ziio.imagehubbackend.entity.UploadPictureResult;
import com.ziio.imagehubbackend.entity.User;
import com.ziio.imagehubbackend.enums.PictureReviewStatusEnum;
import com.ziio.imagehubbackend.exception.BusinessException;
import com.ziio.imagehubbackend.exception.ErrorCode;
import com.ziio.imagehubbackend.exception.ThrowUtils;
import com.ziio.imagehubbackend.manager.CosManager;
import com.ziio.imagehubbackend.manager.FileManager;
import com.ziio.imagehubbackend.manager.upload.FilePictureUpload;
import com.ziio.imagehubbackend.manager.upload.PictureUploadTemplate;
import com.ziio.imagehubbackend.manager.upload.UrlPictureUpload;
import com.ziio.imagehubbackend.request.picture.*;
import com.ziio.imagehubbackend.service.PictureService;
import com.ziio.imagehubbackend.mapper.PictureMapper;
import com.ziio.imagehubbackend.service.SpaceService;
import com.ziio.imagehubbackend.service.UserService;
import com.ziio.imagehubbackend.utils.ColorSimilarUtils;
import com.ziio.imagehubbackend.vo.LoginUserVO;
import com.ziio.imagehubbackend.vo.UserVO;
import com.ziio.imagehubbackend.vo.picutre.PictureVO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
* @author Ziio
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-01-16 21:46:53
*/
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private SpaceService spaceService;

    @Resource
    TransactionTemplate transactionTemplate;

    @Resource
    UserService userService;

    @Resource
    AliYunAiApi aliYunAiApi;

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(inputSource==null,ErrorCode.PARAMS_ERROR);
        // 校验空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 必须空间创建人（管理员）才能上传
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
            // 检验额度
            if(space.getTotalCount() >= space.getMaxCount()){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间已满");
            }
            if(space.getTotalSize() >= space.getMaxSize()){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间大小不足");
            }
        }
        // 用于判断是新增还是更新图片
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 更新图片 ，先判断图片是否存在
        if(pictureId != null){
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture==null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 本人 或 admin 可编辑
            if(!oldPicture.getUserId().equals(loginUser.getId()) && !loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE)){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 校验空间是否一致
            if (spaceId != null) {
                // 沿用 oldPicture.spaceId
                if(oldPicture.getSpaceId()!=null){
                    spaceId = oldPicture.getSpaceId();
                }
            }else{
                // 传了 spaceId ， 必须和原图片一致
                if(ObjectUtil.notEqual(oldPicture.getSpaceId(),spaceId)){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片空间不一致");
                }
            }
            // 图片存在，删除已有图片
            this.clearPictureFile(oldPicture);
        }
        // 上传
        String uploadPathPrefix;
        // 按 spaceId 划分目录
        if (spaceId == null) {
            // 公共目录
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        }else{
            // 私有目录
            uploadPathPrefix = String.format("space/%s", spaceId);
        }
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if(inputSource instanceof String){
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        // 持久化
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        picture.setName(uploadPictureResult.getPicName());
        if(pictureUploadRequest!=null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())){
            picture.setName(pictureUploadRequest.getPicName());
        }
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        picture.setSpaceId(spaceId);
        picture.setPicColor(uploadPictureResult.getPicColor());

        // 补充审核参数
        fillReviewParams(picture, loginUser);
        if(pictureId != null){
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        Long finalSpaceId = spaceId;
        // 开启事务 ，更新额度，不够就连 Picture.save() 一起回滚
        transactionTemplate.execute(status -> {
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
            if(finalSpaceId != null){
                // 更新额度
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize + " + picture.getPicSize())
                        .setSql("totalCount = totalCount + 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "图片上传失败");
            }
            return picture;
        });
        return PictureVO.objToVo(picture);
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if(pictureQueryRequest == null) return queryWrapper;
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 补充审核字段
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage  = pictureQueryRequest.getReviewMessage();
        Long spaceId = pictureQueryRequest.getSpaceId();
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();

        // search word handle
        if(StrUtil.isNotBlank(searchText)){
            queryWrapper.and(qw -> qw.like("name",searchText).or().like("introduction",searchText));
        }
        // handle other args
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(ObjUtil.isNotEmpty(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);

        // search tags handle
        if(CollUtil.isNotEmpty(tags)){
            for(String tag : tags){
                queryWrapper.like("tags","\"" + tag + "\"");
            }
        }
        // order
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField),sortOrder.equals("ascend"),sortField);
        return queryWrapper;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if(CollUtil.isEmpty(pictureList)){
            return pictureVOPage;
        }
        //  填充
        List<PictureVO> voList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        List<Long> userIDList = pictureList.stream().map(Picture::getUserId).collect(Collectors.toList());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIDList).stream().collect(Collectors.groupingBy(User::getId));
        voList.forEach(
                pictureVO -> {
                    Long userId = pictureVO.getUserId();
                    User user = null;
                    if(userIdUserListMap.containsKey(userId)){
                        user = userIdUserListMap.get(userId).get(0);
                    }
                    pictureVO.setUser(userService.getUserVO(user));
                }
        );
        pictureVOPage.setRecords(voList);
        return pictureVOPage;
    }

    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // add userinfo
        Long userId = picture.getUserId();
        if(userId != null && userId >0){
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture==null , ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        ThrowUtils.throwIf(ObjectUtil.isNull(id),ErrorCode.PARAMS_ERROR,"id不为空");
        if(StrUtil.isNotBlank(url)){
            ThrowUtils.throwIf(url.length()>1024,ErrorCode.PARAMS_ERROR,"url过长");
        }
        if(StrUtil.isNotBlank(introduction)){
            ThrowUtils.throwIf(introduction.length()>800,ErrorCode.PARAMS_ERROR,"简介过长");
        }
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum pictureReviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if(id == null || pictureReviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(pictureReviewStatusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 更新审核状态
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest,updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if(userService.isAdmin(loginUser)){
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(new Date());
        }else{
            // 非管理员 ， 待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count>30 , ErrorCode.PARAMS_ERROR , "最多三十条");
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1",searchText);
        Document document = null;
        try{
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败",e);
            throw new RuntimeException(e);
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements elements = div.select("img.mimg");
        int uploadCount = 0;
        for(Element e : elements){
            String fileUrl = e.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            // 处理图片上传地址，防止出现转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            if(StrUtil.isNotBlank(namePrefix)){
                // 设置图片名称
                pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            }
            try{
                PictureVO pictureVO = this.uploadPicture(fileUrl,pictureUploadRequest,loginUser);
                log.info("图片上传成功, id = {}", pictureVO.getId());
                uploadCount++;
            }catch (Exception err){
                log.error("图片上传失败",err);
                continue;
            }
            if(uploadCount >= count){
                break;
            }
        }
        return uploadCount;
    }

    @Resource
    CosManager cosManager;

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 判断图片是否被多条记录使用
        String pictureUrl = oldPicture.getUrl();
        long count = this.lambdaQuery().eq(Picture::getUrl, pictureUrl).count();
        // 多记录 ，为热数据 ，不清理
        if (count > 1) {
            return;
        }
        // 清理原图
        cosManager.deleteObject(oldPicture.getUrl());
        // 清理缩略图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }
    }

    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 公共图库，仅本人或管理员可操作
            if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私有空间，仅空间管理员可操作
            if (!picture.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    @Override
    public void deletePicture(Long id, User loginUser) {
        // 判断是否存在
        Picture picture = this.getById(id);
        ThrowUtils.throwIf(picture == null , ErrorCode.NOT_FOUND_ERROR);
        if(!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 验证空间权限
        checkPictureAuth(loginUser,picture);
        // 开启事务 , space + picture 同步
        transactionTemplate.execute(status -> {
            boolean result = this.removeById(id);
            ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
            // 释放额度
            Long spaceId = picture.getSpaceId();
            if(spaceId != null){
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, spaceId)
                        .setSql("totalSize = totalSize - " + picture.getPicSize())
                        .setSql("totalCount = totalCount - 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return true;
        });
        // 删除图片
        this.clearPictureFile(picture);
    }

    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest,picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        this.validPicture(picture);
        // 判断是否存在
        Long id = pictureEditRequest.getId();
        Picture picture1 = this.getById(id);
        ThrowUtils.throwIf(picture1==null , ErrorCode.NOT_FOUND_ERROR);
        // 验证权限
        checkPictureAuth(loginUser,picture1);
        // 补充审核参数
        this.fillReviewParams(picture,loginUser);
        // save
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result , ErrorCode.OPERATION_ERROR);
    }

    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(StrUtil.isBlank(picColor)||spaceId==null , ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null , ErrorCode.NOT_LOGIN_ERROR);
        // 校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null , ErrorCode.NOT_FOUND_ERROR);
        if (!loginUser.getId().equals(space.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
        }
        // 查询该空间所有图片
        List<Picture> pictures = this.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId)
                .isNotNull(Picture::getPicColor)
                .list();
        if(CollUtil.isEmpty(pictures)){
            return Collections.emptyList();
        }
        // 转换 Color
        Color targetColor = Color.decode(picColor);
        // 计算相似度并排行
        List<Picture> sortedPictures = pictures.stream().sorted(
                Comparator.comparingDouble(picture -> {
                    String hexColor = picture.getPicColor();
                    // 没主色调放到最后
                    if (StrUtil.isBlank(hexColor)) {
                        return Double.MAX_VALUE;
                    }
                    Color pictureColor = Color.decode(hexColor);
                    // 越大越相似
                    return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
                })
        ).limit(12).collect(Collectors.toList());
        // 转换为 VO
        return sortedPictures.stream().map(PictureVO::objToVo).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();
        String nameRule = pictureEditByBatchRequest.getNameRule();
        // 1. 校验参数
        ThrowUtils.throwIf(spaceId == null || CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 2. 校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        if (!loginUser.getId().equals(space.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }
        // 3. 查询主要图片
        List<Picture> pictureList = this.lambdaQuery()
                .select(Picture::getId, Picture::getSpaceId)
                .in(Picture::getId, pictureIdList)
                .eq(Picture::getSpaceId, spaceId)
                .list();
        if (pictureList.isEmpty()) {
            return;
        }
        // 4. 更新分类和标签
        pictureList.forEach(picture -> {
            if (StrUtil.isNotBlank(category)) {
                picture.setCategory(category);
            }
            if (CollUtil.isNotEmpty(tags)) {
                picture.setTags(JSONUtil.toJsonStr(tags));
            }
        });
        // 5. 更新图片名称
        if(StrUtil.isNotBlank(nameRule)){
            fillPictureWithNameRule(pictureList, nameRule);
        }
        // 5. 批量更新
        boolean result = this.updateBatchById(pictureList);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        // 获取图片信息
        Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        Picture picture = Optional.ofNullable(this.getById(pictureId)).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));
        // 权限检验
        checkPictureAuth(loginUser, picture);
        // 构造请求参数
        CreateOutPaintingTaskRequest taskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setImageUrl(picture.getUrl());
        BeanUtil.copyProperties(createPictureOutPaintingTaskRequest, taskRequest);
        // 创建任务
        return aliYunAiApi.createOutPaintingTask(taskRequest);
    }

    /**
     * nameRule 格式: 图片{序号}
     * @param pictureList
     * @param nameRule
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (CollUtil.isEmpty(pictureList) || StrUtil.isBlank(nameRule)) {
            return;
        }
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(pictureName);
            }
        } catch (Exception e) {
            log.error("名称解析错误", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
        }
    }

    @Resource
    private ThreadPoolExecutor customExecutor;

    /**
     * 批量编辑图片分类和标签（异步线程池更新）
     */
    @Transactional
    public void batchEditPictureAsync(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser){
        // 参数校验
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();
        // 1. 校验参数
        ThrowUtils.throwIf(spaceId == null || CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 2. 校验空间权限
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        if (!loginUser.getId().equals(space.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }
        // 3. 查询主要图片
        List<Picture> pictureList = this.lambdaQuery()
                .select(Picture::getId, Picture::getSpaceId)
                .in(Picture::getId, pictureIdList)
                .eq(Picture::getSpaceId, spaceId)
                .list();
        if (pictureList.isEmpty()) {
            return;
        }
        // 分批异步更新
        int batchSize = 100;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for(int i = 0; i < pictureList.size(); i += batchSize){
            List<Picture> batch = pictureList.subList(i, Math.min(i + batchSize, pictureList.size()));
            // 异步更新
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                batch.forEach(picture -> {
                    // 更新标签和分类
                    if (StrUtil.isNotBlank(category)) {
                        picture.setCategory(category);
                    }
                    if (CollUtil.isNotEmpty(tags)) {
                        picture.setTags(JSONUtil.toJsonStr(tags));
                    }
                });
                boolean result = this.updateBatchById(batch);
                if(!result) throw new BusinessException(ErrorCode.OPERATION_ERROR,"批量更新图片失败");
            }, customExecutor);
            futures.add(future);
        }
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}




