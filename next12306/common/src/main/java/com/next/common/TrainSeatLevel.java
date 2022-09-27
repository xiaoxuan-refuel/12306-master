package com.next.common;

import lombok.Getter;

/**
 * @Title: TrainSeatLevel
 * @Description:
 * @author: tjx
 * @date :2022/9/25 23:21
 */
@Getter
public enum TrainSeatLevel {

    TOP_GRADE(0,"特等座/商务座 1排3座"),
    GRADE_1(1,"一等座 1排4座"),
    GRADE_2(2,"二等座 1排5座");

    private Integer level;
    private String desc;

    TrainSeatLevel(Integer level, String desc) {
        this.level = level;
        this.desc = desc;
    }
}
