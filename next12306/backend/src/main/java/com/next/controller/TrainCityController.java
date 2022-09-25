package com.next.controller;

import com.next.common.JsonData;
import com.next.param.TrainCityParam;
import com.next.service.TrainCityService;
import com.next.util.BeanValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Title: TrainCityController
 * @Description:
 * @author: tjx
 * @date :2022/9/22 10:57
 */
@Controller
@RequestMapping("/admin/train/city")
public class TrainCityController {

    @Autowired
    private TrainCityService trainCityService;

    @RequestMapping("/list.page")
    public ModelAndView page(){
        return new ModelAndView("trainCity");
    }

    @RequestMapping("/list.json")
    @ResponseBody
    public JsonData list(){
        return JsonData.success(trainCityService.getAll());
    }

    @RequestMapping("/save.json")
    @ResponseBody
    public JsonData save(TrainCityParam cityParam){
        trainCityService.save(cityParam);
        return JsonData.success();
    }

    @RequestMapping("/update.json")
    @ResponseBody
    public JsonData update(TrainCityParam cityParam){
        trainCityService.update(cityParam);
        return JsonData.success();
    }

}
