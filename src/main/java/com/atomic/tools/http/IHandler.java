package com.atomic.tools.http;

/**
 * @author dreamyao
 * @version 1.0.0
 * @description
 * @Data 2018/05/30 10:48
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
