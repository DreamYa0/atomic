package com.atomic.autotest;

import com.google.common.collect.Lists;
import com.atomic.listener.AnnotationTransformerListener;
import com.atomic.util.ClassUtils;
import org.testng.TestNG;

import java.util.List;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2018/05/30 10:48
 */
public class RunTest {

    /**
     * 通过packagePaths.add("")来添加测试类所在的包路径
     *
     * @param args
     */
    public static void main(String[] args) {
        List<Class<?>> classList = Lists.newArrayList();
        List<String> packagePaths = Lists.newArrayList();
        packagePaths.add("");

        packagePaths.forEach(packagePath -> classList.addAll(ClassUtils.getClasses(packagePath)));
        TestNG testNG = new TestNG();
        testNG.setVerbose(0);
        testNG.setAnnotationTransformer(new AnnotationTransformerListener());
        testNG.setTestClasses(classList.toArray(new Class<?>[]{}));
        testNG.run();
    }
}
