package com.next.param;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @Title: GrabTicketParam
 * @Description:
 * @author: tjx
 * @date :2022/10/7 20:52
 */
@Data
public class GrabTicketParam {
    private Integer fromStationId;
    private Integer toStationId;

    @NotBlank(message = "出发日期不可以为空")
    @Length(max = 8 ,min = 8 ,message = "日期不合法")
    private String date;

    @NotBlank(message = "车次不可以为空")
    private String number;

    @NotBlank(message = "乘车人不可以为空")
    private String travellerIds;
}
