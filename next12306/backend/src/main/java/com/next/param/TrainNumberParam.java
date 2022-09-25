package com.next.param;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @Title: TrainNumberParam
 * @Description:
 * @author: tjx
 * @date :2022/9/24 23:13
 */
@Data
public class TrainNumberParam {

    private Integer id;

    @NotBlank(message = "车次名称不可以为空")
    @Length(min = 2 , max = 10,message = "车次名称长度需要在2-20个字符之间")
    private String name;

    @NotBlank(message = "座位类型不可以为空")
    private String trainType;

    @Min(value = 1,message = "类型不合法")
    @Max(value = 4,message = "类型不合法")
    private Integer type;
}
