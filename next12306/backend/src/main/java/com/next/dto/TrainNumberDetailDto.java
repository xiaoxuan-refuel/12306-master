package com.next.dto;

import com.next.model.TrainNumberDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Title: TrainNumberDetailDto
 * @Description:
 * @author: tjx
 * @date :2022/9/22 22:55
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TrainNumberDetailDto extends TrainNumberDetail {
    private String trainNumber;

    private String fromStation;

    private String toStation;
}
