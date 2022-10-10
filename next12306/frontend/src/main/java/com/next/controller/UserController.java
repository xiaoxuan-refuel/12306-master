package com.next.controller;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.next.common.JsonData;
import com.next.common.OrderStatusEnum;
import com.next.dto.TrainOrderExtDto;
import com.next.model.TrainOrder;
import com.next.model.TrainOrderDetail;
import com.next.model.TrainTraveller;
import com.next.model.TrainUser;
import com.next.service.TrainOrderService;
import com.next.service.TrainStationService;
import com.next.service.TrainTravellerService;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Title: UserController
 * @Description:
 * @author: tjx
 * @date :2022/10/4 13:39
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private TrainTravellerService trainTravellerService;
    @Autowired
    private TrainOrderService trainOrderService;
    @Autowired
    private TrainStationService trainStationService;


    @RequestMapping("/getTravellers.json")
    @ResponseBody
    public JsonData getTravellers(HttpServletRequest request){
        TrainUser user = (TrainUser) request.getSession().getAttribute("user");
        List<TrainTraveller> trainTravellerList = trainTravellerService.getByUserId(user.getId());
        List<TrainTraveller> showList = trainTravellerList.stream().map(
                trainTraveller -> TrainTraveller.builder()
                        .id(trainTraveller.getId())
                        .name(trainTraveller.getName())
                        .adultFlag(trainTraveller.getAdultFlag())
                        .idNumber(hideSensitiveMsg(trainTraveller.getIdNumber()))
                        .build())
                .collect(Collectors.toList());
        return JsonData.success(showList);
    }

    private String hideSensitiveMsg(String msg) {
        if (StringUtils.isBlank(msg) || msg.length() < 7) {
            return msg;
        }
        return msg.substring(0, 3) + "******" + msg.substring(msg.length() - 3);
    }

    @RequestMapping("/getOrderList.json")
    @ResponseBody
    public JsonData getOrderList(HttpServletRequest request){
        TrainUser user = (TrainUser) request.getSession().getAttribute("user");
        List<TrainOrder> orderList = trainOrderService.getOrderList(user.getId());
        if (CollectionUtils.isEmpty(orderList)) {
            return JsonData.success();
        }
        List<TrainTraveller> travellerList = trainTravellerService.getByUserId(user.getId());
        Map<Long, String> travellerNameMap = travellerList.stream().collect(Collectors.toMap(TrainTraveller::getId, TrainTraveller::getName));

        List<String> orderIdList = orderList.stream().map(trainOrder -> trainOrder.getOrderId()).collect(Collectors.toList());
        List<TrainOrderDetail> orderDetailList = trainOrderService.getOrderDetailList(orderIdList);
        //定义一个存储key为 orderId value为Collection<TrainOrderDetail>的数据结构
        Multimap<String,TrainOrderDetail> orderDetailMultiMap = HashMultimap.create();
        orderDetailList.parallelStream().forEach(trainOrderDetail -> orderDetailMultiMap.put(trainOrderDetail.getOrderId(),trainOrderDetail));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd HH:mm");
        ZoneId zoneId = ZoneId.systemDefault();

        List<TrainOrderExtDto> dtoList = orderList.stream().map(trainOrder -> {
            TrainOrderExtDto trainOrderExtDto = new TrainOrderExtDto();
            trainOrderExtDto.setTrainOrder(trainOrder);
            trainOrderExtDto.setFromStationName(trainStationService.getNameById(trainOrder.getFromStationId()));
            trainOrderExtDto.setToStationName(trainStationService.getNameById(trainOrder.getToStationId()));
            trainOrderExtDto.setShowPay(trainOrder.getStatus() == OrderStatusEnum.AWAIT_PAY.getStatus());
            trainOrderExtDto.setShowCancel(trainOrder.getStatus() == OrderStatusEnum.HAVE_PAID.getStatus());
            LocalDateTime startTime = trainOrder.getTrainStart().toInstant().atZone(zoneId).toLocalDateTime();
            LocalDateTime endTime = trainOrder.getTrainEnd().toInstant().atZone(zoneId).toLocalDateTime();
            Collection<TrainOrderDetail> tmpOrderDetailList = orderDetailMultiMap.get(trainOrder.getOrderId());
            trainOrderExtDto.setSeatInfo(dtf.format(startTime) +" ~ "+ dtf.format(endTime) + " " +
                                        generateSeatInfo(tmpOrderDetailList,travellerNameMap) + " " +
                                        "金额("+trainOrder.getTotalMoney()+")元");
            return trainOrderExtDto;
        }).collect(Collectors.toList());
        return JsonData.success(dtoList);
    }

    private String generateSeatInfo(Collection<TrainOrderDetail> tmpOrderDetailList,Map<Long, String> travellerNameMap){
        if (CollectionUtils.isEmpty(tmpOrderDetailList)) {
            return "";
        }
        StringBuilder sb = new StringBuilder(tmpOrderDetailList.size() * 20);
        for (TrainOrderDetail trainOrderDetail : tmpOrderDetailList) {
            if(trainOrderDetail.getTravellerId() == 0 || travellerNameMap.containsKey(trainOrderDetail.getTravellerId())){
                //异常数据
                continue;
            }
            sb.append(travellerNameMap.get(trainOrderDetail.getTravellerId())).append(" ")
                    .append(trainOrderDetail.getCarriageNumber()).append("车")
                    .append(trainOrderDetail.getRowNumber()).append("排")
                    .append(trainOrderDetail.getSeatNumber()).append("座")
                    .append("; ");

        }
        return sb.toString();
    }
}
