package com.next.service;

import com.next.dao.TrainCityMapper;
import com.next.exception.BusinessException;
import com.next.model.TrainCity;
import com.next.param.TrainCityParam;
import com.next.util.BeanValidator;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Title: TrainCityService
 * @Description: 城市管理服务类
 * @author: tjx
 * @date :2022/9/22 22:44
 */
@Service
public class TrainCityService {
    @Autowired
    private TrainCityMapper trainCityMapper;

    public List<TrainCity> getAll(){
        return trainCityMapper.getAll();
    }

    public void save(TrainCityParam param){
        BeanValidator.check(param);
        if(checkExist(param.getName(),param.getId())){
            throw new BusinessException("存在相同名称的城市");
        }
        TrainCity trainCity = TrainCity.builder().name(param.getName()).build();
        trainCityMapper.insert(trainCity);
    }

    public void update(TrainCityParam param){
        BeanValidator.check(param);
        if(checkExist(param.getName(),param.getId())){
            throw new BusinessException("存在相同名称的城市");
        }
        TrainCity trainCityObj = trainCityMapper.selectByPrimaryKey(param.getId());
        if (trainCityObj == null) {
            throw new BusinessException("需更改的城市不存在");
        }
        TrainCity trainCity = TrainCity.builder().id(param.getId()).name(param.getName()).build();
        trainCityMapper.updateByPrimaryKeySelective(trainCity);
    }

    /**
     * 新增：id为空，直接添加
     * 更新：id不为空，查询id不等于传参中的id，并统计是否有重复城市名称
     * @param name
     * @param trainCityId
     * @return
     */
    private Boolean checkExist(String name, Integer trainCityId){
        return trainCityMapper.countByNameAndId(name,trainCityId) > 0;
    }
}
