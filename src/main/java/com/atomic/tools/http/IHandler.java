package com.atomic.tools.http;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @date 2018/05/30 10:48
 */
public interface IHandler {

    /**
     * 处理逻辑
     * @param t excel入参
     * @return 响应结果
     */
    <T> HttpResponse handle(T t, HttpRequest request);

    /**
     * 处理器设置
     * @param handler 处理器
     */
    void setHandler(IHandler handler);
}
