package com.atomic.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 通用并行运行测试用例注解
 * @Data 2017/7/28 16:34
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD})
@Deprecated
public @interface Parallel {
}
