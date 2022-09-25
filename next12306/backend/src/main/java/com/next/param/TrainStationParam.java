package com.next.param;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @Title: TrainStationParam
 * @Description:
 * @author: tjx
 * @date :2022/9/24 23:12
 */
@Data
public class TrainStationParam {

    private Integer id;

    @NotBlank(message = "站点名称不可以为空")
    @Length(min = 2 , max = 20,message = "城市名称长度需要在2-20个字符之间")
    private String name;

    @NotNull(message = "城市不可以为空")
    private Integer cityId;

}
