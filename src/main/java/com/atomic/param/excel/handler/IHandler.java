package com.atomic.param.excel.handler;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 通用处理器模版
 * @date 2018/05/30 10:48
 */
public interface IHandler {

    <T> void handle(T t);

    <T> T handle2Result(T t);

    void setHandler(IHandler handler);
}
