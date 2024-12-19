package com.ziio.imagehubbackend.controller;

import com.ziio.imagehubbackend.common.BaseResponse;
import com.ziio.imagehubbackend.common.ResultUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
public class MainController {
    @GetMapping("/health")
    public BaseResponse<String> health(){
        return ResultUtil.success("ok");
    }
}
