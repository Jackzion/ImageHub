package com.ziio.imagehubbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ziio.imagehubbackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.ziio.imagehubbackend.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ziio.imagehubbackend.entity.User;
import com.ziio.imagehubbackend.request.picture.*;
import com.ziio.imagehubbackend.vo.picutre.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Ziio
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-01-16 21:46:54
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     *
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * request to query
     * @param queryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest queryRequest);

    /**
     * picturePage to pictureVoPage
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage , HttpServletRequest request);

    /**
     * picture to pictureVo
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture , HttpServletRequest request);

    /**
     * 检验图片（大小，url。。。）
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 审核图片
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest , User loginUser);

    /**
     * 自动审核(admin pass)
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture , User loginUser);

    /**
     * 爬虫批次上传图片
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest , User loginUser);

    /**
     * 清理图片云盘
     * @param oldPicture
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 检查space权限
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 删除图片
     * @param id
     * @param loginUser
     */
    void deletePicture(Long id, User loginUser);

    /**
     *
     * 编辑图片
     * @param pictureEditRequest
     * @param loginUser
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 根据颜色搜索图片
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 图片批量编辑
     * @param pictureEditByBatchRequest
     * @param loginUser
     */
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    /**
     * 创建图片任务
     * @param createPictureOutPaintingTaskRequest
     * @param loginUser
     * @return
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
}
