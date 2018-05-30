package com.atomic.listener;

import com.atomic.annotations.AnnotationUtils;
import org.testng.IAnnotationTransformer3;
import org.testng.Reporter;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.IDataProviderAnnotation;
import org.testng.annotations.IFactoryAnnotation;
import org.testng.annotations.IListenersAnnotation;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


/**
 * @author dreamyao
 * @version 1.0.0
 * @title 测试监听器、测试注解处理类
 * @Data 2018/05/30 10:48
 */
public class AnnotationTransformerListener implements IAnnotationTransformer3 {

    /**
     * 动态为测试类注入监听器
     * @param annotation
     * @param testClass
     */
    @Override
    @SuppressWarnings("unchecked")
    public void transform(IListenersAnnotation annotation, Class testClass) {
        try {
            Method method = testClass.getMethod("testCase");
            if (AnnotationUtils.isScenario(method)) {
                annotation.setValue(new Class[]{ScenarioRollBackListener.class, SaveResultListener.class, ReportListener.class});
            } else {
                annotation.setValue(new Class[]{RollBackListener.class, SaveResultListener.class, ReportListener.class});
            }
        } catch (NoSuchMethodException e) {
            Reporter.log("[AnnotationTransformerListener#transform()]:{测试类中没有找到名称为testCase的测试方法！}", true);
            e.printStackTrace();
        }
    }

    @Override
    public void transform(IConfigurationAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {

    }

    /**
     * 修改DataProvider
     * @param annotation
     * @param method
     */
    @Override
    public void transform(IDataProviderAnnotation annotation, Method method) {
        if (AnnotationUtils.isParallel(method)) {
            if (!annotation.isParallel()) {
                annotation.setParallel(true);
            }
        }
    }

    @Override
    public void transform(IFactoryAnnotation annotation, Method method) {

    }

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {

    }
}
