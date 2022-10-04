package com.next.dto;

import lombok.Data;

/**
 * @Title: TrainNumberLeftDto
 * @Description:
 * @author: tjx
 * @date :2022/10/3 16:23
 */
@Data
public class TrainNumberLeftDto {

    private Integer id;//车次Id

    private String number;//车次名称

    private Long leftCount;//剩余座位余票

}
