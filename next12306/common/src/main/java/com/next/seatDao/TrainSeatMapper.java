package com.next.seatDao;

import com.next.model.TrainSeat;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TrainSeatMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TrainSeat record);

    int insertSelective(TrainSeat record);

    TrainSeat selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TrainSeat record);

    int updateByPrimaryKey(TrainSeat record);

    void batchInsert(@Param("list") List<TrainSeat> list);

    List<TrainSeat> searchList(@Param("trainNumberId") Integer trainNumberId,@Param("ticket") String ticket,
                               @Param("carriageNum") Integer carriageNum,@Param("rowNum") Integer rowNum,
                               @Param("seatNum") Integer seatNum, @Param("status") Integer status,
                               @Param("offset") Integer offset,@Param("pageSize") Integer pageSize);

    Integer countList(@Param("trainNumberId") Integer trainNumberId,@Param("ticket") String ticket,
                      @Param("carriageNum") Integer carriageNum,@Param("rowNum") Integer rowNum,
                      @Param("seatNum") Integer seatNum, @Param("status") Integer status);

    Integer batchPublish(@Param("trainNumberId") Integer trainNumberId,@Param("trainSeatIdList") List<Long> trainSeatIdList);
}