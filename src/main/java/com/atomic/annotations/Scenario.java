package com.atomic.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * 通用dubbo接口场景测试注解
 * @author dreamyao
 * @version 1.0
 *          Created by dreamyao on 2017/6/23.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface Scenario {

    /**
     * 被测接口的Class
     * @return
     */
    Class<?> Interface() default Object.class;

    /**
     * 被测接口的方法名
     * @return
     */
    String methodName() default "";

    /**
     * dubbo服务版本号
     * @return
     */
    String version() default "";
}
