package com.next.common;

import lombok.Getter;

/**
 * @Title: TrainType
 * @Description: 定义车次类型对应的车厢数量
 * @author: tjx
 * @date :2022/9/24 23:57
 */
@Getter
public enum TrainType {

    CRH2(1220),
    CRH5(1244);

    int count;

    TrainType(int count) {
        this.count = count;
    }
}
