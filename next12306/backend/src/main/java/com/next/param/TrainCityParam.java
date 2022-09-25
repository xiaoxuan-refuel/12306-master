package com.next.param;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @Title: TrainCityParam
 * @Description:
 * @author: tjx
 * @date :2022/9/24 22:43
 */
@Data
public class TrainCityParam {
    private Integer id;

    @NotBlank(message = "城市名称不可以为空")
    @Length(min = 2 , max = 20,message = "城市名称长度需要在2-20个字符之间")
    private String name;
}
