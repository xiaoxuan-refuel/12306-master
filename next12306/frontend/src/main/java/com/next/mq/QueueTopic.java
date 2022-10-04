package com.next.mq;

/**
 * @Title: QueueTopic
 * @Description: topic常量定义
 * @author: tjx
 * @date :2022/10/4 20:51
 */
public interface QueueTopic {
    Integer TEST = 0;//测试

    Integer SEAT_PLACE_ROLLBACK=1; //座位数据异常回滚操作

    Integer ORDER_CREATE = 2; //订单创建之后需要做的事情(例如：发短信、邮件等)

    Integer ORDER_PAY_DELAY_CHECK =3;//订单延迟支付检查操作

    Integer ORDER_CANCEL = 4;//当订单取消之后需要做的操作(例如：退款，改座位状态等)

    Integer ORDER_PAY_SUCCESS = 5;//支付成功需要做的操作
}
