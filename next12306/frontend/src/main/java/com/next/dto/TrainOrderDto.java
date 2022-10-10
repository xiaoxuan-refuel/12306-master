package com.next.dto;

import com.next.model.TrainOrder;
import com.next.model.TrainOrderDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Title: TrainOrderDto
 * @Description:
 * @author: tjx
 * @date :2022/10/9 17:44
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainOrderDto {

    private TrainOrder trainOrder;

    private List<TrainOrderDetail> trainOrderDetailList;
}
