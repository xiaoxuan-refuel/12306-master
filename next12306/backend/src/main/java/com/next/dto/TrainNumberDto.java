package com.next.dto;

import com.next.model.TrainNumber;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Title: TrainNumberDto
 * @Description:
 * @author: tjx
 * @date :2022/9/22 22:55
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TrainNumberDto extends TrainNumber {

    private String fromStation;
    private String toStation;
}
