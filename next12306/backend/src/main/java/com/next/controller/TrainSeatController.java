package com.next.controller;

import com.google.common.collect.Lists;
import com.next.beans.PageQuery;
import com.next.beans.PageResult;
import com.next.common.JsonData;
import com.next.dto.TrainNumberDto;
import com.next.dto.TrainSeatDto;
import com.next.model.TrainNumber;
import com.next.model.TrainSeat;
import com.next.model.TrainStation;
import com.next.param.GenerateTicketParam;
import com.next.param.PublishTicketParam;
import com.next.param.TrainSeatSearchParam;
import com.next.service.TrainNumberService;
import com.next.service.TrainSeatService;
import com.next.service.TrainStationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Title: TrainSeatController
 * @Description:
 * @author: tjx
 * @date :2022/9/25 23:32
 */
@Controller
@RequestMapping("/admin/train/seat")
public class TrainSeatController {

    @Autowired
    private TrainSeatService trainSeatService;

    @Autowired
    private TrainStationService trainStationService;

    @Autowired
    private TrainNumberService trainNumberService;

    @RequestMapping("/list.page")
    public ModelAndView page(){
        return new ModelAndView("trainSeat");
    }

    @RequestMapping("/search.json")
    @ResponseBody
    public JsonData search(TrainSeatSearchParam param, PageQuery pageQuery){
        Integer totalCount = trainSeatService.countList(param);
        PageResult pageResult = new PageResult();
        if (totalCount == 0) {
            pageResult.setTotal(0);
            return JsonData.success(pageResult);
        }
        List<TrainSeat> trainSeats = trainSeatService.searchList(param, pageQuery);
        if (CollectionUtils.isEmpty(trainSeats)) {
            pageResult.setData(null);
            pageResult.setTotal(0);
            return JsonData.success(pageResult);
        }

        List<TrainStation> stationList = trainStationService.getAll();
        Map<Integer, String> stationMap = stationList.stream().collect(Collectors.toMap(TrainStation::getId, TrainStation::getName));
        List<TrainSeatDto> trainSeatDtoList = Lists.newArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        ZoneId zoneId = ZoneId.systemDefault();
        for (TrainSeat trainSeat : trainSeats) {
            TrainSeatDto trainSeatDto = new TrainSeatDto();
            BeanUtils.copyProperties(trainSeat,trainSeatDto);
            trainSeatDto.setTrainNumber(param.getTrainNumber());
            trainSeatDto.setFromStation(stationMap.get(trainSeat.getFromStationId()));
            trainSeatDto.setToStation(stationMap.get(trainSeat.getToStationId()));
            trainSeatDto.setShowStart(LocalDateTime.ofInstant(trainSeat.getTrainStart().toInstant(),zoneId).format(formatter));
            trainSeatDto.setShowEnd(LocalDateTime.ofInstant(trainSeat.getTrainEnd().toInstant(),zoneId).format(formatter));
            trainSeatDtoList.add(trainSeatDto);
        }
        pageResult.setData(trainSeatDtoList);
        pageResult.setTotal(totalCount);
        return JsonData.success(pageResult);
    }

    @RequestMapping("/generate.json")
    @ResponseBody
    public JsonData generate(GenerateTicketParam param){
        trainSeatService.generate(param);
        return JsonData.success();
    }

    @RequestMapping("/publish.json")
    @ResponseBody
    public JsonData publish(PublishTicketParam param){
        trainSeatService.publish(param);
        return JsonData.success();
    }
}
