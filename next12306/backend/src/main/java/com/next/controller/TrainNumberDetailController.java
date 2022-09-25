package com.next.controller;

import com.google.common.collect.Lists;
import com.next.common.JsonData;
import com.next.dto.TrainNumberDetailDto;
import com.next.dto.TrainNumberDto;
import com.next.model.TrainNumber;
import com.next.model.TrainNumberDetail;
import com.next.model.TrainStation;
import com.next.param.TrainNumberDetailParam;
import com.next.service.TrainCityService;
import com.next.service.TrainNumberDetailService;
import com.next.service.TrainNumberService;
import com.next.service.TrainStationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Title: TrainnNumberDetailController
 * @Description:
 * @author: tjx
 * @date :2022/9/22 10:58
 */
@Controller
@RequestMapping("/admin/train/numberDetail")
public class TrainNumberDetailController {
    @Autowired
    private TrainNumberDetailService trainNumberDetailService;
    @Autowired
    private TrainStationService trainStationService;

    @Autowired
    private TrainNumberService trainNumberService;

    @RequestMapping("/list.page")
    public ModelAndView page(){
        return new ModelAndView("trainNumberDetail");
    }

    @RequestMapping("/list.json")
    @ResponseBody
    public JsonData list(){
        List<TrainNumberDetail> numberDetailList = trainNumberDetailService.getAll();
        List<TrainStation> stationList = trainStationService.getAll();
        Map<Integer, String> stationMap = stationList.stream().collect(Collectors.toMap(TrainStation::getId, TrainStation::getName));
        List<TrainNumber> trainNumberList = trainNumberService.getAll();
        Map<Integer, String> trainNumberMap = trainNumberList.stream().collect(Collectors.toMap(TrainNumber::getId, TrainNumber::getName));
        List<TrainNumberDetailDto> trainNumberDetailDtoList = Lists.newArrayList();
        for (TrainNumberDetail trainNumberDetail : numberDetailList) {
            TrainNumberDetailDto trainNumberDetailDto = new TrainNumberDetailDto();
            BeanUtils.copyProperties(trainNumberDetail,trainNumberDetailDto);
            trainNumberDetailDto.setFromStation(stationMap.get(trainNumberDetail.getFromStationId()));
            trainNumberDetailDto.setToStation(stationMap.get(trainNumberDetail.getToStationId()));
            trainNumberDetailDto.setTrainNumber(trainNumberMap.get(trainNumberDetail.getTrainNumberId()));
            trainNumberDetailDtoList.add(trainNumberDetailDto);
        }
        return JsonData.success(trainNumberDetailDtoList);
    }

    @RequestMapping("/save.json")
    @ResponseBody
    public JsonData save(TrainNumberDetailParam detailParam){
        trainNumberDetailService.save(detailParam);
        return JsonData.success();
    }

    @RequestMapping("/delete.json")
    @ResponseBody
    public JsonData delete(@RequestParam("id") Integer id){
        trainNumberDetailService.delete(id);
        return JsonData.success();
    }
}
