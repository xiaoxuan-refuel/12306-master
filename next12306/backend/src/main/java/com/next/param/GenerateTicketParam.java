package com.next.param;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * @Title: GenerateTicketParam
 * @Description: 给具体某一个车次生成座位参数类
 * @author: tjx
 * @date :2022/9/26 21:27
 */
@Data
public class GenerateTicketParam {

    @NotNull(message = "车次不可以为空")
    private Integer trainNumberId;

    @NotBlank(message = "必须有发车时间")
    private String fromTime;


}
