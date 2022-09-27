package com.next.common;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import javafx.util.Pair;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;
import java.util.Map;

/**
 * @Title: TrainTypeSeatConstant
 * @Description: 维护不同车次类型的座位数据
 * @author: tjx
 * @date :2022/9/25 22:34
 */
public class TrainTypeSeatConstant {
    //车厢,排,座位:每一排从哪一号到哪一号的 from -> to
    private static Table<Integer,Integer, Pair<Integer,Integer>> crh2Table = HashBasedTable.create();
    //车厢,排,每一排的座位数是几个，默认为5，不为5的才存储进来
    private static Table<Integer,Integer, Integer> crh2SpecialTable = HashBasedTable.create();
    //车厢号对应的排数，数组索引下标对应的就是车厢号以及车厢号对应的排数
    private static List<Integer> crh2CarriageRowTotal = Lists.newArrayList(0,11,20,17,20,11,20,13,13,11,20,17,20,11,20,13,13);

    //车厢,排,座位:每一排从哪一号到哪一号的 from -> to
    private static Table<Integer,Integer, Pair<Integer,Integer>> crh5Table = HashBasedTable.create();
    //车厢,排,每一排的座位数是几个，默认为5，不为5的才存储进来
    private static Table<Integer,Integer, Integer> crh5SpecialTable = HashBasedTable.create();
    //车厢号对应的排数，数组索引下标对应的就是车厢号以及车厢号对应的排数
    private static List<Integer> crh5CarriageRowTotal = Lists.newArrayList(0,15,19,19,19,19,9,16,15,15,19,19,19,19,9,16,15);

    //将对应车次类型的座位数据存储到Map中
    private static Map<TrainType,Table<Integer,Integer, Pair<Integer,Integer>>> carriageMap = Maps.newConcurrentMap();

    //存储对应车次类型的具体车厢的座位等级数据
    private static Table<TrainType,Integer,TrainSeatLevel> seatCarriageLevelTable = HashBasedTable.create();
    static {
        //crh2
        for (int row = 1; row <= 12 ; row++) {
            crh2SpecialTable.put(7,row,4);
            crh2SpecialTable.put(15,row,4);
        }
        crh2SpecialTable.put(7,13,3);
        crh2SpecialTable.put(15,13,3);
        crh2SpecialTable.put(8,1,4);
        crh2SpecialTable.put(16,1,4);
        for (int carriage = 1; carriage < crh2CarriageRowTotal.size(); carriage++) { // 遍历每一节车厢
            int order = 0; //保存每一排开始座位号的值 也就是Pair<Integer,Integer>的第一个元素值
            for (int row = 1; row <= crh2CarriageRowTotal.get(carriage); row++) { //遍历每一节车厢的每一排的座位
                int count = 5; //默认每一排的座位为5个
                //如果在遍历过程中，车厢数对应的排数有生成过座位时，这获取对应的座位数并覆盖给count值
                if(crh2SpecialTable.contains(carriage,row)){
                    count = crh2SpecialTable.get(carriage,row);
                }
                //将每一节车厢对应的排的座位号进行填充
                crh2Table.put(carriage,row,new Pair<>(order + 1, order + count));
                order += count;
            }
        }

        //crh5
        for (int row = 1; row <= 15 ; row++) {
            crh5SpecialTable.put(8,row,4);
            crh5SpecialTable.put(16,row,4);
        }
        crh5SpecialTable.put(1,1,4);
        crh5SpecialTable.put(2,1,3);
        crh5SpecialTable.put(3,1,3);
        crh5SpecialTable.put(4,1,3);
        crh5SpecialTable.put(5,1,3);
        crh5SpecialTable.put(6,1,3);
        crh5SpecialTable.put(6,1,4);
        crh5SpecialTable.put(7,1,3);
        crh5SpecialTable.put(7,1,1);
        crh5SpecialTable.put(9,1,4);
        crh5SpecialTable.put(10,1,3);
        crh5SpecialTable.put(11,1,3);
        crh5SpecialTable.put(12,1,3);
        crh5SpecialTable.put(13,1,3);
        crh5SpecialTable.put(14,1,3);
        crh5SpecialTable.put(14,1,4);
        crh5SpecialTable.put(15,1,3);
        crh5SpecialTable.put(15,1,1);

        for (int carriage = 1; carriage < crh5CarriageRowTotal.size(); carriage++) { // 遍历每一节车厢
            int order = 0; //保存每一排开始座位号的值 也就是Pair<Integer,Integer>的第一个元素值
            for (int row = 1; row <= crh5CarriageRowTotal.get(carriage); row++) { //遍历每一节车厢的每一排的座位
                int count = 5; //默认每一排的座位为5个
                //如果在遍历过程中，车厢数对应的排数有生成过座位时，这获取对应的座位数并覆盖给count值
                if(crh5SpecialTable.contains(carriage,row)){
                    count = crh5SpecialTable.get(carriage,row);
                }
                //将每一节车厢对应的排的座位号进行填充
                crh5Table.put(carriage,row,new Pair<>(order + 1, order + count));
                order += count;
            }
        }

        carriageMap.put(TrainType.CRH2,crh2Table);
        carriageMap.put(TrainType.CRH5,crh5Table);

        seatCarriageLevelTable.put(TrainType.CRH2,1,TrainSeatLevel.TOP_GRADE);
        seatCarriageLevelTable.put(TrainType.CRH2,2,TrainSeatLevel.GRADE_1);
        seatCarriageLevelTable.put(TrainType.CRH5,crh5CarriageRowTotal.size() - 1,TrainSeatLevel.TOP_GRADE);
        seatCarriageLevelTable.put(TrainType.CRH5,crh5CarriageRowTotal.size() - 2,TrainSeatLevel.GRADE_1);
    }

    /**
     * 根据车次类型获取对应的座位数据
     * @param trainType
     * @return
     */
    public static Table<Integer,Integer, Pair<Integer,Integer>> getTable(TrainType trainType){
        return carriageMap.get(trainType);
    }

    /**
     * 根据车次类型以及车厢号 获取对应的座位等级
     * @param trainType
     * @param carriage
     * @return
     */
    public static TrainSeatLevel getSeatLevel(TrainType trainType,Integer carriage){
        if (seatCarriageLevelTable.contains(trainType,carriage)) {
            return seatCarriageLevelTable.get(trainType,carriage);
        }
        return TrainSeatLevel.GRADE_2;
    }
}
