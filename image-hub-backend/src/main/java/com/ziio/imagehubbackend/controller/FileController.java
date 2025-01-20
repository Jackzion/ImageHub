package com.ziio.imagehubbackend.controller;

import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import com.ziio.imagehubbackend.common.BaseResponse;
import com.ziio.imagehubbackend.common.ResultUtil;
import com.ziio.imagehubbackend.exception.BusinessException;
import com.ziio.imagehubbackend.exception.ErrorCode;
import com.ziio.imagehubbackend.manager.CosManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RequestMapping("/file")
@RestController
@Slf4j
public class FileController {

    @Resource
    CosManager cosManager;

    @PostMapping("test/upload")
    public BaseResponse<String> testUpload(@RequestPart("file")MultipartFile multipartFile){
        // 文件目录
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s",filename);
        File file = null;
        try {
            file = File.createTempFile(filepath,null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath,file);
            return ResultUtil.success(filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    @GetMapping("test/download")
    public void testDownloadFile(String filepath , HttpServletResponse response) throws IOException {
        COSObjectInputStream cosObjectInputStream = null;
        try{
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInputStream= cosObject.getObjectContent();
            // 处理流
            byte[] byteArray = IOUtils.toByteArray(cosObjectInputStream);
            // 设置响应头
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("file download error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInputStream != null) {
                cosObjectInputStream.close();
            }
        }
    }
}
