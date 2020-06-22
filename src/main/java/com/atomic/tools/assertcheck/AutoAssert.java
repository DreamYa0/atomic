package com.atomic.tools.assertcheck;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * 智能化断言注解
 * @author dreamyao
 * @version 1.0
 *          Created by dreamyao on 2017/6/18.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface AutoAssert {

    CheckMode checkMode() default CheckMode.NORMAL;
}
