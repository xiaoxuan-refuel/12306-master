package com.next.controller;

import com.next.common.JsonData;
import com.next.dto.TrainNumberLeftDto;
import com.next.exception.BusinessException;
import com.next.param.SearchLeftCountParam;
import com.next.service.TrainSeatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.json.Json;
import java.util.List;

/**
 * @Title: FrontController
 * @Description:
 * @author: tjx
 * @date :2022/10/3 16:19
 */
@Controller
@RequestMapping("/front")
@Slf4j
public class FrontController {
    @Autowired
    private TrainSeatService trainSeatService;

    @RequestMapping("/searchLeftCount.json")
    @ResponseBody
    public JsonData searchLeftCount(SearchLeftCountParam param){
        try {
            List<TrainNumberLeftDto> dtoList = trainSeatService.searchLeftCount(param);
            return JsonData.success(dtoList);
        } catch (Exception e) {
            log.error("searchLeftCount exception param: {}",param,e);
            throw new BusinessException("车次查询异常，请稍后尝试");
        }
    }
}
