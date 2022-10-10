package com.next.common;

import lombok.Getter;

/**
 * @Title: OrderStatusEnum
 * @Description:
 * @author: tjx
 * @date :2022/10/10 10:43
 */
@Getter
public enum OrderStatusEnum {
    AWAIT_PAY(10,"已占座等待支付"),
    HAVE_PAID(20,"已支付"),
    TIME_OUT_NON_PAY(30,"超时未支付自动取消"),
    HAVE_PAID_REFUND(40,"支付后退款")
    ;

    private int status;
    private String desc;

    OrderStatusEnum(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
