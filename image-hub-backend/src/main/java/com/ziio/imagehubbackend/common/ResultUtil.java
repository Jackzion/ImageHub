package com.ziio.imagehubbackend.common;

import com.ziio.imagehubbackend.exception.ErrorCode;

public class ResultUtil {

    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"OK");
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }

    public static <T> BaseResponse<T> error(int code , String message){
        return new BaseResponse<>(code,null,message);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode , String message){
        return new BaseResponse<>(errorCode.getCode(),null,message);
    }

}
