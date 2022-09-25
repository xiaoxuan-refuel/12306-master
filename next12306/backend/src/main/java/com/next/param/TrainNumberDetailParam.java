package com.next.param;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @Title: TrainNumberDetailParam
 * @Description:
 * @author: tjx
 * @date :2022/9/24 23:13
 */
@Data
public class TrainNumberDetailParam {

    /**
     * 车次id
     */
    @NotNull(message = "车次不可以为空")
    private Integer trainNumberId;

    /**
     * 出发站
     * ps: 车次详情中的fromStation和toStation表示的是 出发站 -> 到达站，
     *     而在车次列表中fromStation和toStation表示的是 始发站 -> 终点站
     */
    @NotNull(message = "出发站不可以为空")
    private Integer fromStationId;

    /**
     * 到达站
     */
    @NotNull(message = "到达站不可以为空")
    private Integer toStationId;

    /**
     * 从一个站到另一个站需要的时间
     */
    @NotNull(message = "相对出发时间不可以为空")
    private Integer relativeMinute;

    /**
     * 到达一个站之后需要等多长时间
     */
    @NotNull(message = "等待时间不可以为空")
    private Integer waitMinute;

    /**
     * 从一个站到另一个站区间不同座位的价钱
     */
    @NotNull(message = "座位价钱不可以为空")
    private String money;

    /**
     * 车次的结尾标识
     */
    @Min(0)  //当值为0时，则表示该车次还需继续添加详情
    @Max(1)  //当值为1时，则表示本次车次详情全部添加完成
    private Integer end;

}
