package com.ziio.imagehubbackend.exception;

import com.ziio.imagehubbackend.common.BaseResponse;
import com.ziio.imagehubbackend.common.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e){
        log.error("BusinessException" , e);
        return ResultUtil.error(e.getCode(),e.getMessage());
    }
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e){
        log.error("RuntimeException" , e);
        return ResultUtil.error(ErrorCode.SYSTEM_ERROR,ErrorCode.SYSTEM_ERROR.getMessage());
    }

}
