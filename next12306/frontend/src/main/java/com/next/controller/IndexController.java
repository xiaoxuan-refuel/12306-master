package com.next.controller;

import com.next.common.JsonData;
import com.next.dao.TrainStationMapper;
import com.next.model.TrainUser;
import com.next.service.TrainStationService;
import org.apache.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @Title: IndexController
 * @Description: 用户前台登录/退出类
 * @author: tjx
 * @date :2022/10/3 15:10
 */
@Controller
public class IndexController {
    @Autowired
    private TrainStationService trainStationService;

    @RequestMapping("/")
    public ModelAndView index(){
        return new ModelAndView("index");
    }

    @RequestMapping("/mockLogin.json")
    @ResponseBody
    public JsonData mockLogin(HttpServletRequest request){
        TrainUser trainUser = TrainUser.builder().id(1l).name("test").build();
        trainUser.setPassword(null);
        request.getSession().setAttribute("user",trainUser);
        return JsonData.success();
    }

    @RequestMapping("/logout.json")
    @ResponseBody
    public JsonData logout(HttpServletRequest request){
        request.getSession().invalidate();
        return JsonData.success();
    }

    //获取车站站点信息
    @RequestMapping("/stationList.json")
    @ResponseBody
    public JsonData stationList(){
        return JsonData.success(trainStationService.getAll());
    }

    //获取用户基本信息
    @RequestMapping("/info.json")
    @ResponseBody
    public JsonData info(HttpServletRequest request){
        return JsonData.success(request.getSession().getAttribute("user"));
    }
}
