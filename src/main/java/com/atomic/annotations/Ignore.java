package com.atomic.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * 测试函数的注解
 * @author huangling
 * @title xxx
 * @Data 2016/8/29.
 * @since v1.0.0
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface Ignore {

}
