package com.next.controller;

import com.google.common.collect.Lists;
import com.next.common.JsonData;
import com.next.dto.TrainStationDto;
import com.next.model.TrainCity;
import com.next.model.TrainStation;
import com.next.param.TrainStationParam;
import com.next.service.TrainCityService;
import com.next.service.TrainStationService;
import org.apache.commons.collections.ArrayStack;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Title: TrainSeatController
 * @Description:
 * @author: tjx
 * @date :2022/9/22 10:57
 */
@Controller
@RequestMapping("/admin/train/station")
public class TrainStationController {

    @Autowired
    private TrainStationService trainStationService;

    @Autowired
    private TrainCityService trainCityService;

    @RequestMapping("/list.page")
    public ModelAndView page(){
        return new ModelAndView("trainStation");
    }

    @RequestMapping("/list.json")
    @ResponseBody
    public JsonData list(){
        List<TrainStation> trainStationList = trainStationService.getAll();
        List<TrainCity> cityList = trainCityService.getAll();
        Map<Integer, String> collect = cityList.stream().collect(Collectors.toMap(TrainCity::getId, TrainCity::getName));
        List<TrainStationDto> stationDtoList = Lists.newArrayList();
        for (TrainStation trainStation : trainStationList) {
            TrainStationDto trainStationDto = new TrainStationDto();
            BeanUtils.copyProperties(trainStation,trainStationDto);
            String cityName = collect.get(trainStation.getCityId());
            trainStationDto.setCityName(cityName);
            stationDtoList.add(trainStationDto);
        }
        return JsonData.success(stationDtoList);
    }

    @RequestMapping("/save.json")
    @ResponseBody
    public JsonData save(TrainStationParam stationParam){
        trainStationService.save(stationParam);
        return JsonData.success();
    }

    @RequestMapping("/update.json")
    @ResponseBody
    public JsonData update(TrainStationParam stationParam){
        trainStationService.update(stationParam);
        return JsonData.success();
    }
}
