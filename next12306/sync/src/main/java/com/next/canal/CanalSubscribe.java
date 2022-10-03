package com.next.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;
import com.next.service.TrainNumberService;
import com.next.service.TrainSeatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @Title: CanalSubscribe
 * @Description: canal异步拉取mysql数据处理类
 * @author: tjx
 * @date :2022/9/27 23:30
 */
@Component
@Slf4j
public class CanalSubscribe implements ApplicationListener<ApplicationContextEvent> {

    @Autowired
    private TrainNumberService trainNumberService;
    @Autowired
    private TrainSeatService trainSeatService;

    @Override
    public void onApplicationEvent(ApplicationContextEvent applicationContextEvent) {
        new Thread(()->{
            canalSubscribe();
        }).start();
    }

    public void canalSubscribe() {
        log.info("canal start subscribe");
        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress("192.168.3.235",
                11111), "train", "", "");
        int batchSize = 1000;
        try {
            connector.connect();
            connector.subscribe(".*\\..*");
            connector.rollback();
            while (true) {
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                try {
                    log.info("new message [batchId=%s,size=%s]", batchId, size);
                    printEntry(message.getEntries());
                    connector.ack(batchId); // 提交确认
                } catch (CanalClientException e) {
                    log.error("canal data handle exception, batchId:{} ",batchId,e);
                    connector.rollback(batchId); // 处理失败, 回滚数据
                }
            }
        } catch (Exception e){
            log.error("canal subscribe exception",e);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
            }
            canalSubscribe();
        }
    }

    private void printEntry(List<CanalEntry.Entry> entrys) throws Exception {
        for (CanalEntry.Entry entry : entrys) {
            //如果当前操作处于事务之间，则直接进入下一次遍历
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            CanalEntry.RowChange rowChange = null;
            try {
                rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("RowChange.parse exception , data:" + entry.toString(),e);
            }

            CanalEntry.EventType eventType = rowChange.getEventType();
//            String logfileName = entry.getHeader().getLogfileName(); //binlog文件名称
//            long logfileOffset = entry.getHeader().getLogfileOffset(); // binlog的偏移量
            String schemaName = entry.getHeader().getSchemaName();//库名称
            String tableName = entry.getHeader().getTableName();//表名
            log.info("schemaName: {}, tableName: {},eventType: {}",schemaName,tableName,eventType);

            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                if (eventType == CanalEntry.EventType.DELETE) {
                    //如果sql操作为del操作 则获取删除当前的数据记录
                    handleColumn(rowData.getBeforeColumnsList(),eventType,schemaName,tableName);
                }
//                else if (eventType == CanalEntry.EventType.INSERT) {
//                    //如果sql操作为insert操作，则获取当前新增之后的数据记录
//                    handleColumn(rowData.getAfterColumnsList(),eventType,schemaName,tableName);
//                }
                else {
//                    System.out.println("-------&gt; before");
//                    printColumn(rowData.getBeforeColumnsList());
//                    System.out.println("-------&gt; after");
                    handleColumn(rowData.getAfterColumnsList(),eventType,schemaName,tableName);
                }
            }
        }
    }

    /**
     * @param columns 本次操作的表字段信息
     * @param eventType 操作类型
     * @param schemaName 数据库名称
     * @param tableName 表名称
     */
    private void handleColumn(List<CanalEntry.Column> columns,CanalEntry.EventType eventType,String schemaName,String tableName) throws Exception {
        if(schemaName.contains("train_seat")){
            //处理座位数据的变更
            trainSeatService.handle(columns,eventType);
        }else if(tableName.equals("train_number")){
            //处理车次数据变更信息 当车次表数据有变更，则直接在车次详情中获取当前所有的车次信息，并做处理
            trainNumberService.handle(columns,eventType);
        }else{
            log.info("data drop, no need care");
        }
    }


}
