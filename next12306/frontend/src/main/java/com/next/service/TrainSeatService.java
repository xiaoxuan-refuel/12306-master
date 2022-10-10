package com.next.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.next.common.TrainEsConstant;
import com.next.common.TrainSeatLevel;
import com.next.common.TrainType;
import com.next.common.TrainTypeSeatConstant;
import com.next.dto.RollbackSeatDto;
import com.next.dto.TrainNumberLeftDto;
import com.next.dto.TrainOrderDto;
import com.next.exception.BusinessException;
import com.next.exception.ParamException;
import com.next.model.*;
import com.next.mq.MessageBody;
import com.next.mq.QueueTopic;
import com.next.mq.RabbitMqClient;
import com.next.orderDao.TrainOrderDetailMapper;
import com.next.orderDao.TrainOrderMapper;
import com.next.param.GrabTicketParam;
import com.next.param.SearchLeftCountParam;
import com.next.seatDao.TrainSeatMapper;
import com.next.util.BeanValidator;
import com.next.util.JsonMapper;
import com.next.util.StrUtil;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.jackson.type.TypeReference;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Title: TrainSeatService
 * @Description:
 * @author: tjx
 * @date :2022/10/3 16:25
 */
@Service
@Slf4j
public class TrainSeatService {
    //这里线程池采用不允许丢弃任务的拒绝策略，CallerRunsPolicy拒绝策略当工作队列已满、线程数已满，该拒绝策略会将新任务交给提交任务的线程去执行
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,20,
            2, TimeUnit.MINUTES,new ArrayBlockingQueue<>(200),new ThreadPoolExecutor.CallerRunsPolicy());

    @Autowired
    private EsClient esClient;
    @Autowired
    private TrainCacheService trainCacheService;
    @Autowired
    private TrainNumberService trainNumberService;
    @Autowired
    private TrainSeatMapper trainSeatMapper;
    @Autowired
    private RabbitMqClient rabbitMqClient;
    @Autowired
    private TrainOrderMapper trainOrderMapper;
    @Autowired
    private TrainOrderDetailMapper trainOrderDetailMapper;
    @Autowired
    private TransactionService transactionService;

    public List<TrainNumberLeftDto> searchLeftCount(SearchLeftCountParam param) throws Exception{
        BeanValidator.check(param);
        List<TrainNumberLeftDto> dtoList = Lists.newArrayList();
        //先根据出发站点和到达站点id取ES中的车次信息
        //因为这里只需涉及到一次请求，所以不需要MultiGetRequest,使用GetRequest即可
        GetRequest getRequest = new GetRequest(TrainEsConstant.INDEX,TrainEsConstant.TYPE,param.getFromStationId() +"_"+ param.getToStationId());
        GetResponse getResponse = esClient.get(getRequest);
        Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
        //当根据出发站点和到达站点 无法在ES中获取到数据时，直接返回空数据即可
        if(CollectionUtils.isEmpty(sourceAsMap)){
            return dtoList;
        }
        //取出对应的出发站点 ->到达站点的车次信息，因在ES中存储的是D123,D456这种格式，所以需要进行拆分
        String trainNumbers = (String) sourceAsMap.get(TrainEsConstant.COLUMN_TRAIN_NUMBER);
        List<String> trainNumberList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(trainNumbers);
        trainNumberList.parallelStream().forEach(number -> {
            TrainNumber trainNumber = trainNumberService.findByNameFromCache(number);
            if (trainNumber == null) {
                return;
            }
            String detailStr = trainCacheService.get("TN_" + number);
            List<TrainNumberDetail> detailList = JsonMapper.string2Obj(detailStr, new TypeReference<List<TrainNumberDetail>>() {
            });
            //这里将车次详情数据转成Map结构，key -> fromStationId , value -> detail
            Map<Integer,TrainNumberDetail> detailMap = Maps.newHashMap();
            detailList.stream().forEach(detail-> detailMap.put(detail.getFromStationId(),detail));
            /** 这个Map的作用如下：
             * 根据前台的传参 出发站 到  到达站点时，依次查询出从出发站开始 一直查询到toStationId为到达站点停止，
             * 且根据这些车断 查询若干个车断中间的座位余票，并取出余票数最小的数
             * 例如：2 ->{2(fromStationId),3(toStationId)}, 3 -> {3,4} -> 4 {4,5}
             * 当前台查询 2 - 5的车次信息 及余票时，需要查询 {2,3},{3,4},{4,5}车断中的座位余票，并取出三个车断中余票数最少的余票数展示
             * 为什么要取最小的呢？
             * 假如{2,3}:剩余5张余票 ,{3,4}:剩余3张余票, {4,5}:剩余10张余票
             * 在2到5中所有车断中的通用座位就只有三个，假如展示余票为5，2-5这个车次中最多也只能卖3张。
             */
            int fromStationId = param.getFromStationId();
            int targetToStationId = param.getToStationId();
            long min = Long.MAX_VALUE;
            Boolean isSuccess = Boolean.FALSE;
            String redisKey = trainNumber.getName()+"_"+param.getDate()+"_count";
            while(true){
                TrainNumberDetail trainNumberDetail = detailMap.get(fromStationId);
                if (trainNumberDetail == null) {
                    log.error("detail is null, stationId:{}, number:{}", fromStationId, number);
                    break;
                }
                //根据车次及时间信息、fromStationId和toStationId去redis中查询对应车次的座位余票
                min = Math.min(min,NumberUtils.toLong(trainCacheService.hget(redisKey,trainNumberDetail.getFromStationId()+"_"+trainNumberDetail.getToStationId()),00));
                if (trainNumberDetail.getToStationId() == targetToStationId) {
                    isSuccess = Boolean.TRUE;
                    break;
                }
                fromStationId = trainNumberDetail.getToStationId();
            }
            if(isSuccess){
                TrainNumberLeftDto trainNumberLeftDto = new TrainNumberLeftDto();
                trainNumberLeftDto.setId(trainNumber.getId());
                trainNumberLeftDto.setNumber(number);
                trainNumberLeftDto.setLeftCount(min);
                dtoList.add(trainNumberLeftDto);
            }
        });
        return dtoList;
    }

    public TrainOrderDto grabTicket(GrabTicketParam param, TrainUser trainUser){
        BeanValidator.check(param);
        //乘车人
        List<Long> travellerIds = StrUtil.splitToListLong(param.getTravellerIds());
        if (CollectionUtils.isEmpty(travellerIds)) {
            throw new ParamException("需选择乘车人");
        }
        //车次信息
        TrainNumber trainNumber = trainNumberService.findByNameFromCache(param.getNumber());
        if (trainNumber == null) {
            throw new ParamException("车次信息异常");
        }
        //拿出当前车次总的车次详情数据
        String detailStr = trainCacheService.get("TN_" + param.getNumber());
        List<TrainNumberDetail> detailList = JsonMapper.string2Obj(detailStr, new TypeReference<List<TrainNumberDetail>>() {
        });
        //这里将车次详情数据转成Map结构，key -> fromStationId , value -> detail
        Map<Integer,TrainNumberDetail> detailMap = Maps.newHashMap();
        detailList.stream().forEach(detail-> detailMap.put(detail.getFromStationId(),detail));

        //该集合存储的是车次详情的实际子级，也就是拿到具体车站到另一个车站的车次详情
        List<TrainNumberDetail> targetTrainNumberDetailList = Lists.newArrayList();
        int fromStationId = param.getFromStationId();
        int targetToStationId = param.getToStationId();
        while(true){
            TrainNumberDetail trainNumberDetail = detailMap.get(fromStationId);
            if (trainNumberDetail == null) {
                log.error("detail is null, stationId:{}, number:{}", fromStationId, param.getNumber());
                break;
            }
            targetTrainNumberDetailList.add(trainNumberDetail);
            if (trainNumberDetail.getToStationId() == targetToStationId) {
                break;
            }
            fromStationId = trainNumberDetail.getToStationId();
        }

        //实际车次的座位情况(获取当前车次及对应时间的座位具体情况)
        Map<String, String> seatMap = trainCacheService.hgetAll(param.getNumber() + "_" + param.getDate());

        //当前车次类型的座位布局
        TrainType trainType = TrainType.valueOf(trainNumber.getTrainType());
        Table<Integer, Integer, Pair<Integer, Integer>> seatTable = TrainTypeSeatConstant.getTable(trainType);

        String parentOrderId = UUID.randomUUID().toString();//主订单号
        List<TrainOrderDetail> orderDetailList = Lists.newArrayList();//具体的订单详情(可包含多个乘客的订单)
        List<TrainSeat> trainSeatList = Lists.newArrayList();//最终实际抢到的座位
        Integer totalMoney = 0;
        for (Long travellerId : travellerIds) { //给每一个乘客生成一个合适的座位，并组成一个订单
            //获取筛选出合适的座位
            TrainSeat tmpTrainSeat = selectOneMatch(seatTable, seatMap, targetTrainNumberDetailList, trainNumber, travellerId, trainUser.getId(),param.getDate());
            if (tmpTrainSeat == null) {
                break;
            }
            TrainSeatLevel seatLevel = TrainTypeSeatConstant.getSeatLevel(trainType, tmpTrainSeat.getCarriageNumber());
            //生成订单详情
            TrainOrderDetail trainOrderDetail = TrainOrderDetail.builder()
                                                .parentOrderId(parentOrderId)
                                                .orderId(UUID.randomUUID().toString())
                                                .travellerId(travellerId)
                                                .userId(trainUser.getId())
                                                .trainNumberId(trainNumber.getId())
                                                .carriageNumber(tmpTrainSeat.getCarriageNumber())
                                                .rowNumber(tmpTrainSeat.getRowNumber())
                                                .seatNumber(tmpTrainSeat.getSeatNumber())
                                                .seatLevel(seatLevel.getLevel())
                                                .fromStationId(tmpTrainSeat.getFromStationId())
                                                .toStationId(tmpTrainSeat.getToStationId())
                                                .ticket(param.getDate())
                                                .trainStart(tmpTrainSeat.getTrainStart())
                                                .trainEnd(tmpTrainSeat.getTrainEnd())
                                                .money(tmpTrainSeat.getMoney())
                                                .createTime(new Date())
                                                .updateTime(new Date())
                                                .expireTime(DateUtils.addMinutes(new Date(),30))
                                                .status(10)
                                                .build();
            totalMoney += tmpTrainSeat.getMoney();
            orderDetailList.add(trainOrderDetail);
            trainSeatList.add(tmpTrainSeat);
        }
        //如果当前返回的座位数小于乘车人数时 回滚座位及车次详情数据，最终抛出业务级别异常
        if(trainSeatList.size() < travellerIds.size()){
            rollbackPlace(trainSeatList,targetTrainNumberDetailList);
            throw new BusinessException("座位不足");
        }
        //生成主订单
        TrainOrder trainOrder = TrainOrder.builder()
                                          .orderId(parentOrderId)
                                          .ticket(param.getDate())
                                          .totalMoney(totalMoney)
                                          .userId(trainUser.getId())
                                          .trainNumberId(trainNumber.getId())
                                          .fromStationId(param.getFromStationId())
                                          .toStationId(param.getToStationId())
                                          .trainStart(orderDetailList.get(0).getTrainStart())
                                          .trainEnd(orderDetailList.get(0).getTrainEnd())
                                          .createTime(new Date())
                                          .updateTime(new Date())
                                          .expireTime(DateUtils.addMinutes(new Date(),30))
                                          .status(10)
                                          .build();
        //保存订单详情和主订单(该地方需要保证两者的事务)
        try {
            transactionService.saveOrder(trainOrder,orderDetailList);
        } catch (Exception e) {
            rollbackPlace(trainSeatList,targetTrainNumberDetailList);
            log.error("saveOrder exception ,trainOrder: {} , orderDetail: {}",trainOrder,orderDetailList,e);
            throw new BusinessException("订单保存失败！");
        }
        log.info("saveOrder success , trainOrder: {}",trainOrder);
        //给消息队列发送一个保存订单成功的消息，比如可以给用户发短信等；
        MessageBody messageBody1 = new MessageBody();
        messageBody1.setTopic(QueueTopic.ORDER_CREATE);
        messageBody1.setDetail(JsonMapper.obj2String(trainOrder));
        rabbitMqClient.send(messageBody1);
        //订单支付延迟检查消息 发给延迟消息队列，当支付时间过去之后，检查座位状态，如果没有进行支付，需将座位进行回滚
        MessageBody messageBody2 = new MessageBody();
        messageBody2.setTopic(QueueTopic.ORDER_PAY_DELAY_CHECK);
        messageBody2.setDetail(JsonMapper.obj2String(trainOrder));
        messageBody2.setDelay(30 * 60 * 1000);
        rabbitMqClient.sendDelay(messageBody2,30 * 60 * 1000);
        //返回核心数据
        return TrainOrderDto.builder()
                            .trainOrder(trainOrder)
                            .trainOrderDetailList(orderDetailList)
                            .build();
    }

    //该方法弃用，且因为这种写法为错误的，会导致事务失效
    @Transactional(rollbackFor = Exception.class)
    public void saveOrder(TrainOrder trainOrder,List<TrainOrderDetail> trainOrderDetailList){
        for (TrainOrderDetail trainOrderDetail : trainOrderDetailList) {
            trainOrderDetailMapper.insertSelective(trainOrderDetail);
        }
        trainOrderMapper.insertSelective(trainOrder);
    }

    //筛选出合适的座位 车厢/排/座位号 且该座位在车厢必须是空着的，最终完成占座
    private TrainSeat selectOneMatch(Table<Integer, Integer, Pair<Integer, Integer>> seatTable,
                                     Map<String, String> seatMap,
                                     List<TrainNumberDetail> targetTrainNumberDetailList,
                                     TrainNumber trainNumber,
                                     Long travellerId,Long userId,String ticket){
        for (Table.Cell<Integer, Integer, Pair<Integer, Integer>> cell : seatTable.cellSet()) { //遍历每一节车厢的每一排
            Integer carriage = cell.getRowKey(); //当前遍历的车厢
            Integer row = cell.getColumnKey(); //当前遍历的车厢的排
            Pair<Integer,Integer> rowSeatRange = seatTable.get(carriage,row);//获取具体每一排的座位号范围
            for (int i = rowSeatRange.getKey(); i <= rowSeatRange.getValue() ; i++) {
                Integer count = 0;
                for (TrainNumberDetail trainNumberDetail : targetTrainNumberDetailList) {
                    String seatRedisKey = carriage + "_" + row + "_" + i + "_" +trainNumberDetail.getFromStationId() +"_"+trainNumberDetail.getToStationId();
                    //如果当前座位在seatMap中不存在 说明该座位没有放票 或者该座位的占座状态值不为0 表示该座位被占座
                    if(!seatMap.containsKey(seatRedisKey) || NumberUtils.toInt(seatMap.get(seatRedisKey),0) != 0){
                        break;
                    }
                    count++;
                }
                //如果count值 等于详情的数据大小，表示该座位在这段车次详情区间空闲，可以尝试占座
                if(count == targetTrainNumberDetailList.size()){
                    //构建出一个初始的座位信息
                    TrainSeat trainSeat = TrainSeat.builder()
                                                    .carriageNumber(carriage)
                                                    .rowNumber(row)
                                                    .seatNumber(i)
                                                    .trainNumberId(trainNumber.getId())
                                                    .travellerId(travellerId)
                                                    .userId(userId)
                                                    .build();
                    try {
                        //调用place方法返回综合的座位信息
                        trainSeat = place(trainSeat,targetTrainNumberDetailList,ticket);
                        if (trainSeat != null) {
                            //如果返回的综合座位信息不为空，则遍历车次详情对应的座位进行占座
                            for (TrainNumberDetail trainNumberDetail : targetTrainNumberDetailList) {
                                String seatRedisKey = carriage + "_" + row + "_" + i + "_" +trainNumberDetail.getFromStationId() +"_"+trainNumberDetail.getToStationId();
                                seatMap.put(seatRedisKey,"1");
//                                trainCacheService.hset();
                            }
                            log.info("place success trainSeat:{}",trainSeat);
                            return trainSeat;
                        }
                    } catch (BusinessException e) {
                        //如果抛出了业务级别的异常，表示该座位不满足条件(可能已经被占座等原因)，此时可以继续尝试获取其他座位信息,并且将本次座位保存到redis中，避免下次继续遍历到该座位
                        log.error("place BusinessException {},{}",trainSeat,e.getMessage());
                        for (TrainNumberDetail trainNumberDetail : targetTrainNumberDetailList) {
                            String seatRedisKey = carriage + "_" + row + "_" + i + "_" +trainNumberDetail.getFromStationId() +"_"+trainNumberDetail.getToStationId();
                            seatMap.put(seatRedisKey,"1");
                        }
                    } catch (Exception e) {
                        //如果抛出了非业务级别的异常，直接返回空数据。不执行后续逻辑
                        log.error("place Exception {},{}",trainSeat,e);
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**----------占座---------
     * 需将合适的座位，在多个详情段中进行统一占座(因为在抢票过程中，站与站之间是可能会跨多个详情的，因此需要在多个车次详情中统一占座)
     * 且该方法返回的座位信息，是一个综合之后的信息  例子如下
     * 假如我们抢票的始发站和到达站是 北京 - 锦州，而在这个过程中分为多个区间段依次为 北京-唐山 唐山-锦州
     * 假如从北京到唐山需要一小时且座位价格是100，唐山到锦州需要两小时且座位价格为200
     * 因此该方法返回的座位就是一个综合信息，北京-锦州 =====》 需要3小时，座位价格为300
     * @param trainSeat
     * @param targetTrainNumberDetailList
     * @return
     */
    private TrainSeat place(TrainSeat trainSeat,List<TrainNumberDetail> targetTrainNumberDetailList,String ticket){
        List<Integer> fromStationIdList = targetTrainNumberDetailList.stream().map(TrainNumberDetail::getFromStationId).collect(Collectors.toList());
        List<TrainSeat> toUpdatePlaceSeatList = trainSeatMapper.getToPlaceSeatList(trainSeat.getTrainNumberId(), trainSeat.getCarriageNumber(),
                                                                             trainSeat.getRowNumber(), trainSeat.getSeatNumber(), fromStationIdList,ticket);
        //如果查询返回的座位总数不等于车次详情返回的fromStationIds集合数，说明某些座位可能以及被占座，直接返回null值，让其继续遍历后续的座位
        if (toUpdatePlaceSeatList.size() != fromStationIdList.size()) {
            return null;
        }
        List<Long> idList = toUpdatePlaceSeatList.stream().map(TrainSeat::getId).collect(Collectors.toList());
        int row = trainSeatMapper.batchUpdateSeat(trainSeat.getTrainNumberId(), idList, trainSeat.getTravellerId(), trainSeat.getUserId());
        //如果返回的行数 与之前查询出来的座位总数不等，先回滚之前修改的座位数据状态，直接抛出业务异常表示座位以及被占座。
        if (row != idList.size()) {
            rollbackPlace(toUpdatePlaceSeatList,targetTrainNumberDetailList);
            throw new BusinessException("座位被占: "+ trainSeat);
        }
        trainSeat.setTrainStart(toUpdatePlaceSeatList.get(0).getTrainStart());
        trainSeat.setTrainEnd(toUpdatePlaceSeatList.get(toUpdatePlaceSeatList.size() - 1).getTrainEnd());
        trainSeat.setMoney(toUpdatePlaceSeatList.stream().collect(Collectors.summingInt(TrainSeat::getMoney)));
        return trainSeat;
    }

    //回滚占座
    private void rollbackPlace(List<TrainSeat> trainSeatList,List<TrainNumberDetail> targetTrainNumberDetailList){
        //使用线程池异步化执行回滚操作，回滚操作用户是不需要关心的，因此使用异步处理，尽可能快速返回用户一个状态或结果
        threadPoolExecutor.submit(()->{
            for (TrainSeat trainSeat : trainSeatList) {
                log.info("rollback seat : {}",trainSeat);
                List<Integer> fromStationIdList = targetTrainNumberDetailList.stream().map(TrainNumberDetail::getFromStationId).collect(Collectors.toList());
                batchRollbackSeat(trainSeat,fromStationIdList,0);
            }
        });
    }

    //执行具体的回滚操作
    public void batchRollbackSeat(TrainSeat trainSeat,List<Integer> fromStationIdList,int delayMillSeconds){
        try {
            trainSeatMapper.batchRollbackPlace(trainSeat,fromStationIdList);
        } catch (Exception e) {
            //当执行具体回滚操作时抛出异常，则需要将这个异常捕获，并将本次的座位信息，以及FromStationId信息统一发送给MQ
            log.error("batchRollbackSeat exception, seat :{}",trainSeat,e);
            RollbackSeatDto dto = new RollbackSeatDto();
            dto.setTrainSeat(trainSeat);
            dto.setFromStationIdList(fromStationIdList);
            MessageBody messageBody = new MessageBody();
            messageBody.setTopic(QueueTopic.SEAT_PLACE_ROLLBACK);
            messageBody.setDetail(JsonMapper.obj2String(dto));
            //这里避免一直异常将消息不断发送给MQ
            //这里避免发送消息不断进行DB操作，因此这里设置的延迟时间间隔会越来越大
            //下面时间及那个为：2，4，6，8....
            delayMillSeconds = delayMillSeconds + 2 * 1000;
            messageBody.setDelay(delayMillSeconds);
            //TODO 这里有一个可优化的点，为了避免频繁将消息发送给MQ导致MQ服务不可用，可以写给一个本地内存Queue去处理
            // 当本地内存Queue队列满了，可以考虑将消息写入到一个文件，让一个线程或者分布式定时任务定期去扫描指定的文件
            rabbitMqClient.sendDelay(messageBody,delayMillSeconds);
        }
    }
}
