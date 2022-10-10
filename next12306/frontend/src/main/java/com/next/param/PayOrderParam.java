package com.next.param;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @Title: PayOrderParam
 * @Description:
 * @author: tjx
 * @date :2022/10/10 15:58
 */
@Data
public class PayOrderParam {

    @NotBlank(message = "订单id不可以为空")
    private String orderId;
}
