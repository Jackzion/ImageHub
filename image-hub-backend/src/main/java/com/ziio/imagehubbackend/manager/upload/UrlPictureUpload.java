package com.ziio.imagehubbackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.ziio.imagehubbackend.exception.BusinessException;
import com.ziio.imagehubbackend.exception.ErrorCode;
import com.ziio.imagehubbackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        validUrlPicture(fileUrl);
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 从 URL 中提取文件名
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        // 下载文件到临时目录
        HttpUtil.downloadFile(fileUrl, file);
    }

    /**
     * 校验 url 文件
     * @param fileUrl
     */
    private void validUrlPicture(String fileUrl) {
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
