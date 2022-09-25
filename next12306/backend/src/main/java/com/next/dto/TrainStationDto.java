package com.next.dto;

import com.next.model.TrainStation;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Title: TrainSetationDto
 * @Description:
 * @author: tjx
 * @date :2022/9/22 22:55
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TrainStationDto extends TrainStation {
    private String cityName;
}
