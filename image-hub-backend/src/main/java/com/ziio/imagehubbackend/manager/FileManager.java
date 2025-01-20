package com.ziio.imagehubbackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.ziio.imagehubbackend.config.CosClientConfig;
import com.ziio.imagehubbackend.entity.UploadPictureResult;
import com.ziio.imagehubbackend.exception.BusinessException;
import com.ziio.imagehubbackend.exception.ErrorCode;
import com.ziio.imagehubbackend.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.print.DocFlavor;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
@Deprecated
public class FileManager {

    @Resource
    CosManager cosManager;

    @Resource
    CosClientConfig cosClientConfig;

    /**
     * 上传图片
     * @param multipartFile
     * @param uploadPathPrefix
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile , String uploadPathPrefix){
        validPicture(multipartFile);
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s",uploadPathPrefix,uploadFilename);
        File file = null;
        try{
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装 picture info
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            return uploadPictureResult;
        } catch (IOException e) {
            log.error("图片上传到对象存储失败", e);
            throw new RuntimeException(e);
        }finally {
            this.deleteTempFile(file);
        }
    }

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        // 删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }

    /**
     * 校验文件
     */
    public void validPicture(MultipartFile multipartFile){
        ThrowUtils.throwIf(multipartFile == null , ErrorCode.PARAMS_ERROR,"文件不为空");
        long size = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(size > 10 * ONE_M , ErrorCode.PARAMS_ERROR,"文件大小不能超过 2M");
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

    public UploadPictureResult uploadPictureByUrl(String fileUrl , String uploadPathPrefix){
        validPicture(fileUrl);
        // file name
        String uuid = RandomUtil.randomString(16);
        String originalFilename = FileUtil.mainName(fileUrl);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s",uploadPathPrefix,uploadFilename);
        File file = null;
        try{
            // 创建临时文件
            file = File.createTempFile(uploadPath,null);
            // 下载到本地
            HttpUtil.downloadFile(fileUrl,file);
            // 上传本地文件
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 封装 picture info
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            return uploadPictureResult;
        } catch (IOException e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }finally {
            this.deleteTempFile(file);
        }
    }

    /**
     * 校验 url 文件
     * @param fileUrl
     */
    private void validPicture(String fileUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl),ErrorCode.PARAMS_ERROR,"文件地址不为空");
        try{
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件地址格式不正确");
        }
        // 校验 url 协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) , ErrorCode.PARAMS_ERROR , "仅支持 http 协议" );
        // 发送 HEAD 请求验证文件是否存在
        HttpResponse response = null;
        try{
            response = HttpUtil.createRequest(Method.HEAD , fileUrl).execute();
            // 未正常返回
            if(response.getStatus() != HttpStatus.HTTP_OK){
                return;
            }
            // 验证文件类型
            String contentType = response.header("Content-Type");
            if(StrUtil.isNotBlank(contentType)){
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg","image/jpg","image/png","image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),ErrorCode.PARAMS_ERROR);

            }
            // 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if(StrUtil.isNotBlank(contentLengthStr)){
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long TWO_MB = 2 * 1024 * 1024L;
                    ThrowUtils.throwIf(contentLength > TWO_MB , ErrorCode.PARAMS_ERROR,  "文件不能超过 2M");
                }catch (NumberFormatException e){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件大小格式错误");
                }
            }
        }finally {
            if(response!=null){
                response.close();
            }
        }
    }

}
