package com.next.controller;


import com.next.common.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Title: TestController
 * @Description:
 * @author: tjx
 * @date :2022/9/19 15:31
 */
@RestController
@RequestMapping("/test")
public class TestController {


    @GetMapping("/get")
    public JsonData get(){
        return JsonData.success();
    }
}
