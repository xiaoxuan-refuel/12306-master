package com.next.service;

import com.next.common.TrainType;
import com.next.dao.TrainNumberMapper;
import com.next.exception.BusinessException;
import com.next.model.TrainNumber;
import com.next.model.TrainStation;
import com.next.param.TrainNumberParam;
import com.next.util.BeanValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Title: TrainNumberService
 * @Description:
 * @author: tjx
 * @date :2022/9/22 22:45
 */
@Service
public class TrainNumberService {

    @Autowired
    private TrainNumberMapper trainNumberMapper;

    public List<TrainNumber> getAll(){
        return trainNumberMapper.getAll();
    }

    public void save(TrainNumberParam numberParam){
        BeanValidator.check(numberParam);
        TrainNumber trainNumber = trainNumberMapper.findByName(numberParam.getName());
        if (trainNumber != null) {
            throw new BusinessException("该车次已存在");
        }
        //TODO 车次刚开始创建时 是不知道始发站和终点站的，因此这些值我们直接在车次详情中进行填充
        trainNumber = TrainNumber.builder().name(numberParam.getName())
                                           .trainType(numberParam.getTrainType())
                                           .type(numberParam.getType().shortValue())
                                           .seatNum(TrainType.valueOf(numberParam.getTrainType()).getCount())//通过车次类型来获取对应的车厢数量
                                           .build();
        trainNumberMapper.insertSelective(trainNumber);
    }

    public void update(TrainNumberParam numberParam){
        BeanValidator.check(numberParam);
        TrainNumber trainNumber = trainNumberMapper.findByName(numberParam.getName());
        if (trainNumber != null && trainNumber.getId().intValue() != numberParam.getId().intValue()) {
            throw new BusinessException("该车次已存在");
        }
        //可以考虑当seat数量以及生成时，不建议修改。在12306的业务中，一旦新增了一个数据，一般情况下是不太建议修改的，修改之后可能会带来一些其他数据的影响
        trainNumber = TrainNumber.builder().name(numberParam.getName())
                .id(numberParam.getId())
                .trainType(numberParam.getTrainType())
                .type(numberParam.getType().shortValue())
                .seatNum(TrainType.valueOf(numberParam.getTrainType()).getCount())//通过车次类型来获取对应的车厢数量
                .build();
        trainNumberMapper.updateByPrimaryKeySelective(trainNumber);
    }


}
