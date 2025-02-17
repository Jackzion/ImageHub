package com.ziio.imagehubbackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.db.PageResult;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ziio.imagehubbackend.annotation.AuthCheck;
import com.ziio.imagehubbackend.common.BaseResponse;
import com.ziio.imagehubbackend.common.DeleteRequest;
import com.ziio.imagehubbackend.common.ResultUtil;
import com.ziio.imagehubbackend.constant.UserConstant;
import com.ziio.imagehubbackend.entity.Picture;
import com.ziio.imagehubbackend.entity.PictureTagCategory;
import com.ziio.imagehubbackend.entity.Space;
import com.ziio.imagehubbackend.entity.User;
import com.ziio.imagehubbackend.enums.PictureReviewStatusEnum;
import com.ziio.imagehubbackend.exception.BusinessException;
import com.ziio.imagehubbackend.exception.ErrorCode;
import com.ziio.imagehubbackend.exception.ThrowUtils;
import com.ziio.imagehubbackend.request.picture.*;
import com.ziio.imagehubbackend.service.PictureService;
import com.ziio.imagehubbackend.service.SpaceService;
import com.ziio.imagehubbackend.service.UserService;
import com.ziio.imagehubbackend.vo.picutre.PictureVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequestMapping("/picture/")
@RestController
public class PictureController {

    @Resource
    UserService userService;

    @Resource
    PictureService pictureService;

    @Resource
    SpaceService spaceService;

    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file")MultipartFile multipartFile,
            PictureUploadRequest uploadRequest ,
            HttpServletRequest request
            )
    {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile,uploadRequest,loginUser);
        return ResultUtil.success(pictureVO);
    }

    @PostMapping("/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(
            PictureUploadRequest uploadRequest ,
            HttpServletRequest request
    )
    {
        User loginUser = userService.getLoginUser(request);
        String url = uploadRequest.getFileUrl();
        ThrowUtils.throwIf(StrUtil.isBlank(url),ErrorCode.PARAMS_ERROR);
        PictureVO pictureVO = pictureService.uploadPicture(url,uploadRequest,loginUser);
        return ResultUtil.success(pictureVO);
    }

    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest , HttpServletRequest request){
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null , ErrorCode.OPERATION_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest,loginUser);
        return ResultUtil.success(uploadCount);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest , HttpServletRequest request){
        if(deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long id = deleteRequest.getId();
        pictureService.deletePicture(id,loginUser);
        return ResultUtil.success(true);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,HttpServletRequest request){
        if(pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest,picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        pictureService.validPicture(picture);
        // 判断是否存在
        Long id = pictureUpdateRequest.getId();
        Picture byId = pictureService.getById(id);
        ThrowUtils.throwIf(byId == null , ErrorCode.NOT_FOUND_ERROR);
        // 补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture,loginUser);
        // 操作数据库
        boolean result = pictureService.updateById(byId);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return ResultUtil.success(true);
    }

    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id , HttpServletRequest request){
        ThrowUtils.throwIf(id <= 0 , ErrorCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null , ErrorCode.NOT_FOUND_ERROR);
        return ResultUtil.success(picture);
    }

    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id , HttpServletRequest request){
        ThrowUtils.throwIf(id <= 0 , ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null , ErrorCode.NOT_FOUND_ERROR);
        // 空间权限校验
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            User loginUser = userService.getLoginUser(request);
            pictureService.checkPictureAuth(loginUser, picture);
        }
        PictureVO pictureVO = pictureService.getPictureVO(picture, request);
        return ResultUtil.success(pictureVO);
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest queryRequest){
        long current = queryRequest.getCurrent();
        long pageSize = queryRequest.getPageSize();
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(queryRequest));
        return ResultUtil.success(picturePage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest queryRequest , HttpServletRequest request){
        long current = queryRequest.getCurrent();
        long pageSize = queryRequest.getPageSize();
        // 空间权限校验
        Long spaceId = queryRequest.getSpaceId();
        if(spaceId == null){
            // 普通用户默认公开已过审
            queryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            queryRequest.setNullSpaceId(true);
        }else{
            // 私有空间
            User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
        }
        queryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(queryRequest));
        return ResultUtil.success(pictureService.getPictureVOPage(picturePage,request));
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest , HttpServletRequest request){
        if(pictureEditRequest == null || pictureEditRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.editPicture(pictureEditRequest,loginUser);
        return ResultUtil.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory(){
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtil.success(pictureTagCategory);
    }

    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest , HttpServletRequest request){
        ThrowUtils.throwIf(pictureReviewRequest==null , ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest,loginUser);
        return ResultUtil.success(true);
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final Cache<String,String> LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(1024)
            .maximumSize(10000L)
            .expireAfterWrite(5L,TimeUnit.SECONDS)
            .build();

    @PostMapping("list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache
            (@RequestBody PictureQueryRequest  pictureQueryRequest,
             HttpServletRequest request)
    {
        long current = pictureQueryRequest.getCurrent();
        long pageSize = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        // 普通用户默认只能查看已过审的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 构建缓存 Key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = "imagehub:listPictureVOByPage:" + hashKey;
        // Caffeine 缓存查询
        String CaffeineCache = LOCAL_CACHE.getIfPresent(cacheKey);
        if (CaffeineCache != null) {
            // 缓存命中，返回结果
            Page<PictureVO> cachePage = JSONUtil.toBean(CaffeineCache, Page.class);
            return ResultUtil.success(cachePage);
        }
        // Redis 缓存查询
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String redisCache = valueOperations.get(cacheKey);
        if (redisCache != null) {
            // 缓存命中，更新本地缓存，返回结果
            LOCAL_CACHE.put(cacheKey,redisCache);
            Page<PictureVO> cachePage = JSONUtil.toBean(redisCache, Page.class);
            return ResultUtil.success(cachePage);
        }
        // 缓存未命中，查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(pictureQueryRequest));
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        // 将结果存入缓存
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);
        LOCAL_CACHE.put(cacheKey, cacheValue);
        valueOperations.set(cacheKey, cacheValue, 5, TimeUnit.MINUTES);
        // 返回结果
        return ResultUtil.success(pictureVOPage);
    }
}
