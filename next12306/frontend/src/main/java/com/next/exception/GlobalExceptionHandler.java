package com.next.exception;

import com.next.common.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Title: GlobalExceptionHandler
 * @Description: 全局异常处理类
 * @author: tjx
 * @date :2022/9/19 22:04
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    public JsonData exceptionHandler(RuntimeException ex){
        log.error("unknown exception ",ex);
        if(ex instanceof ParamException || ex instanceof BusinessException){
            return JsonData.fail(ex.getMessage());
        }
        return JsonData.fail("系统异常，请稍后重试");
    }

    @ExceptionHandler(value = Error.class)
    public JsonData exceptionHandler(Error ex){
        log.error("unknown error ",ex);
        return JsonData.fail("系统异常，请联系管理员");
    }
}
