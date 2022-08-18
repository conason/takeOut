package com.conason.reggie.controller;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;


/**
 * web后端返回对象
 * @param <T>
 */
@Data
public class Request<T> {

    private Integer code; //编码：1成功，0和其它数字为失败

    private String msg; //错误信息

    private T data; //数据

    private Map map = new HashMap(); //动态数据

    public static <T> Request<T> success(T object) {
        Request<T> req = new Request<T>();
        req.data = object;
        req.code = 1;
        return req;
    }

    public static <T> Request<T> error(String msg) {
        Request req = new Request();
        req.msg = msg;
        req.code = 0;
        return req;
    }

    public Request<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
