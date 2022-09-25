package com.next.controller;

import com.google.common.collect.Lists;
import com.next.common.JsonData;
import com.next.dto.TrainNumberDto;
import com.next.dto.TrainStationDto;
import com.next.model.TrainNumber;
import com.next.model.TrainStation;
import com.next.param.TrainNumberParam;
import com.next.service.TrainNumberService;
import com.next.service.TrainStationService;
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
 * @Title: TrainNumberController
 * @Description:
 * @author: tjx
 * @date :2022/9/22 10:57
 */
@Controller
@RequestMapping("/admin/train/number")
public class TrainNumberController {

    @Autowired
    private TrainNumberService trainNumberService;

    @Autowired
    private TrainStationService trainStationService;

    @RequestMapping("/list.page")
    public ModelAndView page(){
        return new ModelAndView("trainNumber");
    }

    @RequestMapping("/list.json")
    @ResponseBody
    public JsonData list(){
        List<TrainNumber> trainNumberList = trainNumberService.getAll();
        List<TrainStation> stationList = trainStationService.getAll();
        Map<Integer, String> stationMap = stationList.stream().collect(Collectors.toMap(TrainStation::getId, TrainStation::getName));
        List<TrainNumberDto> trainNumberDtoList = Lists.newArrayList();
        for (TrainNumber trainNumber : trainNumberList) {
            TrainNumberDto trainNumberDto = new TrainNumberDto();
            BeanUtils.copyProperties(trainNumber,trainNumberDto);
            trainNumberDto.setFromStation(stationMap.get(trainNumber.getFromStationId()));
            trainNumberDto.setToStation(stationMap.get(trainNumber.getToStationId()));
            trainNumberDtoList.add(trainNumberDto);
        }
        return JsonData.success(trainNumberDtoList);
    }

    @RequestMapping("/save.json")
    @ResponseBody
    public JsonData save(TrainNumberParam numberParam){
        trainNumberService.save(numberParam);
        return JsonData.success();
    }

    @RequestMapping("/update.json")
    @ResponseBody
    public JsonData update(TrainNumberParam numberParam){
        trainNumberService.update(numberParam);
        return JsonData.success();
    }
}
