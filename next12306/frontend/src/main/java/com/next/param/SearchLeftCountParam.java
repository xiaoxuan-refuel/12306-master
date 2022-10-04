package com.next.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @Title: SearchLeftCountParam
 * @Description:
 * @author: tjx
 * @date :2022/10/3 16:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchLeftCountParam {

    private Integer fromStationId;
    private Integer toStationId;
    @NotBlank(message = "出发日期不可以为空")
    @Length(max = 8 ,min = 8 ,message = "日期不合法")
    private String date;
}
