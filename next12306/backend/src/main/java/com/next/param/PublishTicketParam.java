package com.next.param;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @Title: PublishTicketParam
 * @Description: 座位放票参数类
 * @author: tjx
 * @date :2022/9/27 14:51
 */
@Data
public class PublishTicketParam {

    @NotBlank(message = "车次不可以为空")
    private String trainNumber;

    @NotBlank(message = "必须选择座位")
    private String trainSeatIds;
}
