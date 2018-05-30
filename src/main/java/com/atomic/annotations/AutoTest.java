package com.atomic.annotations;

import com.atomic.enums.AutoTestMode;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.atomic.enums.AutoTestMode.SINGLE;
import static com.atomic.param.Constants.MAX_TEST_CASES;
import static java.lang.annotation.ElementType.METHOD;

/**
 * @author dreamyao
 * @version 1.0.0
 *          Created by dreamyao on 2017/6/17.
 * @title 开启自动化测试注解
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface AutoTest {

    /**
     * 最大测试用例数，超过就不启动自动测试
     * @return
     */
    int maxTestCases() default MAX_TEST_CASES;

    /**
     * 属性之间使用多属性模式还是单属性模式
     * @return
     */
    AutoTestMode autoTestMode() default SINGLE;

    /**
     * 自动化测试中途是否打印入参和出参
     * @return
     */
    boolean autoTestPrintResult() default true;

}
