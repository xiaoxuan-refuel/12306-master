package com.next.param;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @Title: CancelOrderParam
 * @Description:
 * @author: tjx
 * @date :2022/10/10 15:59
 */
@Data
public class CancelOrderParam {

    @NotBlank(message = "订单Id不可以为空")
    private String orderId;

}
