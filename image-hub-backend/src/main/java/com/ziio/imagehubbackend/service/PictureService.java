package com.ziio.imagehubbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ziio.imagehubbackend.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ziio.imagehubbackend.entity.User;
import com.ziio.imagehubbackend.request.picture.PictureQueryRequest;
import com.ziio.imagehubbackend.request.picture.PictureReviewRequest;
import com.ziio.imagehubbackend.request.picture.PictureUploadRequest;
import com.ziio.imagehubbackend.vo.picutre.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author Ziio
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-01-16 21:46:54
*/
public interface PictureService extends IService<Picture> {
    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest queryRequest);

    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage , HttpServletRequest request);

    PictureVO getPictureVO(Picture picture , HttpServletRequest request);

    void validPicture(Picture picture);

    void doPictureReview(PictureReviewRequest pictureReviewRequest , User loginUser);

    public void fillReviewParams(Picture picture , User loginUser);
}
