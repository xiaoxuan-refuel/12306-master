package com.next.dto;

import com.next.model.TrainSeat;
import lombok.*;

import java.io.Serializable;

/**
 * @Title: TrainSeatDto
 * @Description:
 * @author: tjx
 * @date :2022/9/26 23:19
 */
@ToString
public class TrainSeatDto extends TrainSeat {

    @Getter
    @Setter
    private String trainNumber; //车次号

    @Getter
    @Setter
    private String fromStation; //出发站

    @Getter
    @Setter
    private String toStation;//到达站

    @Getter
    @Setter
    private String showStart;//出发时间

    @Getter
    @Setter
    private String showEnd; //到达时间


}
