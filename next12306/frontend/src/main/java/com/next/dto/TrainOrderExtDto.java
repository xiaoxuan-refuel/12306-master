package com.next.dto;

import com.next.model.TrainOrder;
import lombok.Data;

/**
 * @Title: TrainOrderExtDto
 * @Description:
 * @author: tjx
 * @date :2022/10/10 15:29
 */
@Data
public class TrainOrderExtDto {

    private TrainOrder trainOrder;

    private String fromStationName;

    private String toStationName;

    private Boolean showPay;

    private Boolean showCancel;

    private String seatInfo;

}
