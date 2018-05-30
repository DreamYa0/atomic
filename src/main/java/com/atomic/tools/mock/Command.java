package com.atomic.tools.mock;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 命令接口
 * @Data 2017 10 03 00:27
 */
public interface Command {

    /**
     * 执行无返回值命令
     * @param t   入参
     * @param <T> 类型
     */
    <T> void execute(T t);

    /**
     * 执行有返回值命令
     * @param t   入参
     * @param <T> 类型
     * @return 结果
     */
    <T> T execute2Result(T t);

    /**
     * 撤销命令
     * @param t   入参
     * @param <T> 类型
     */
    <T> void unExecute(T t);
}
