package com.next.dto;

import com.next.model.TrainSeat;
import lombok.Data;

import java.util.List;

/**
 * @Title: RollbackSeatDto
 * @Description:
 * @author: tjx
 * @date :2022/10/8 23:10
 */
@Data
public class RollbackSeatDto {

    private TrainSeat trainSeat;

    private List<Integer> fromStationIdList;
}
