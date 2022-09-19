package com.next.exception;

/**
 * @Title: ParamException
 * @Description: 处理参数业务异常
 * @author: tjx
 * @date :2022/9/19 22:01
 */
public class ParamException extends RuntimeException{

    public ParamException() {
        super();
    }

    public ParamException(String message) {
        super(message);
    }

    public ParamException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParamException(Throwable cause) {
        super(cause);
    }

    protected ParamException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
