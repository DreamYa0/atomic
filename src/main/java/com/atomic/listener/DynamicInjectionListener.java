package com.atomic.listener;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestNGMethod;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 动态的为测试套件注入监听器
 * @Data 2018/05/30 10:48
 */
public class DynamicInjectionListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        injectListener(suite, (ITestNGMethod method) -> method.getMethodsDependedUpon().length > 0);
    }

    @Override
    public void onFinish(ISuite suite) {

    }

    /**
     * 具体注入那种数据回滚监听器
     * @param suite
     * @param predicate 谓词
     */
    private void injectListener(ISuite suite, Predicate<ITestNGMethod> predicate) {
        List<ITestNGMethod> methods = suite.getAllMethods();
        methods.parallelStream().forEach(method -> {
            if (predicate.test(method)) {
                suite.addListener(new ScenarioRollBackListener());
            } else {
                suite.addListener(new RollBackListener());
            }
        });
    }
}
