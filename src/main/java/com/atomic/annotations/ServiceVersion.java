package com.atomic.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Created by dreamyao on 2017/6/13.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface ServiceVersion {

    /**
     * dubbo服务版本号
     * @return
     */
    String version() default "";
}
