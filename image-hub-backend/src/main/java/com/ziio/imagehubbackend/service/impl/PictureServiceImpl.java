package com.ziio.imagehubbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ziio.imagehubbackend.entity.Picture;
import com.ziio.imagehubbackend.entity.UploadPictureResult;
import com.ziio.imagehubbackend.entity.User;
import com.ziio.imagehubbackend.exception.ErrorCode;
import com.ziio.imagehubbackend.exception.ThrowUtils;
import com.ziio.imagehubbackend.manager.FileManager;
import com.ziio.imagehubbackend.request.picture.PictureQueryRequest;
import com.ziio.imagehubbackend.request.picture.PictureUploadRequest;
import com.ziio.imagehubbackend.service.PictureService;
import com.ziio.imagehubbackend.mapper.PictureMapper;
import com.ziio.imagehubbackend.service.UserService;
import com.ziio.imagehubbackend.vo.UserVO;
import com.ziio.imagehubbackend.vo.picutre.PictureVO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Ziio
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-01-16 21:46:53
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    FileManager fileManager;

    @Resource
    UserService userService;
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 用于判断是新增还是更新图片
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }
        // 更新图片 ，先判断图片是否存在
        if(pictureId != null){
            boolean exists = this.lambdaQuery().eq(Picture::getId,pictureId).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        }
        // 上传
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        // 持久化
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());

        if(pictureId != null){
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
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
}




