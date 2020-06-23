package com.atomic.runner;

import org.testng.ITestNGMethod;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @date 2018/05/30 10:48
 */
public interface IHandler {

    Boolean handle(ITestNGMethod testNGMethod);

    void setNextHandler(IHandler handler);
}
