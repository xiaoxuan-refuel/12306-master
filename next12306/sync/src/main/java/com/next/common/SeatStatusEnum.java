package com.next.common;

import lombok.Getter;

/**
 * @Title: SeatStatusEnum
 * @Description:
 * @author: tjx
 * @date :2022/9/29 15:14
 */
@Getter
public enum SeatStatusEnum {
    PUT_TICKET(1,"座位已放票"),
    SCRAMBLE_TICKET(2,"座位已占座,等待支付"),
    PAY_TICKET(3,"座位已占座且支付"),
    NO_PUT_TICKET(4,"座位不外放"),
    ;

    private int status;
    private String desc;

    SeatStatusEnum(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }
}
