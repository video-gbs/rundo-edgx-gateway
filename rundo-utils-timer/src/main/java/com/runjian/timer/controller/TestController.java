package com.runjian.timer.controller;

import com.runjian.common.config.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author Miracle
 * @date 2022/4/26 10:31
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {


    @GetMapping("/get")
    public CommonResponse getTest(){
        return CommonResponse.success();
    }

    @PostMapping("/post")
    public CommonResponse postTest(){
        return CommonResponse.success();
    }

    @PutMapping("/put")
    public CommonResponse putTest(){
        return CommonResponse.success();
    }

    @DeleteMapping("/delete")
    public CommonResponse deleteTest(){
        return CommonResponse.success();
    }
}
