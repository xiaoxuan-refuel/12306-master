package com.next.param;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @Title: TrainSeatSearchParam
 * @Description: 座位列表查询参数类
 * @author: tjx
 * @date :2022/9/26 22:46
 */
@Data
public class TrainSeatSearchParam {

    /**
     * 车次名称
     */
    @NotBlank(message = "车次不可以为空")
    @Length(min = 2,max = 20,message = "车次长度必须在2-20个字符之间")
    private String trainNumber;

    /**
     * 车次出发时间
     */
    @NotBlank(message = "出发时间不可以为空")
    @Length(min = 8,max = 8,message = "出发时间格式必须为yyyyMMdd")
    private String ticket;

    /**
     * 车厢号
     */
    private Integer carriageNum;

    /**
     * 排
     */
    private Integer rowNum;

    /**
     * 座位号
     */
    private Integer seatNum;

    /**
     * 座位状态
     */
    private Integer status;

}
