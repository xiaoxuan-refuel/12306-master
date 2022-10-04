package com.next.controller;

import com.next.common.JsonData;
import com.next.model.TrainTraveller;
import com.next.model.TrainUser;
import com.next.service.TrainTravellerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
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
}
