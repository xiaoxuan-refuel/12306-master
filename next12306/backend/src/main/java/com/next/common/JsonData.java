package com.next.common;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @Title: JsonData
 * @Description: 数据统一返回格式
 * @author: tjx
 * @date :2022/9/19 21:51
 */
@Getter
@Setter
public class JsonData {
    private final static Integer SYSTEM_ERROR = 1;//默认为1 表示失败
    private String msg;
    private Boolean ret;
    private Object data;
    private Integer code = 0; //默认0代表成功

    public JsonData(boolean ret){
        this.ret =ret;
    }

    public static JsonData success(){
        return new JsonData(true);
    }

    public static JsonData success(Object data){
        JsonData jsonData = new JsonData(true);
        jsonData.setData(data);
        return jsonData;
    }

    public static JsonData success(String msg,Object data){
        JsonData jsonData = new JsonData(true);
        jsonData.setData(data);
        jsonData.setMsg(msg);
        return jsonData;
    }

    public static JsonData fail(String msg){
        JsonData jsonData = new JsonData(false);
        jsonData.setMsg(msg);
        jsonData.setCode(SYSTEM_ERROR);
        return jsonData;
    }

    public static JsonData fail(String msg,Integer code){
        JsonData jsonData = new JsonData(false);
        jsonData.setMsg(msg);
        jsonData.setCode(code);
        return jsonData;
    }
}
