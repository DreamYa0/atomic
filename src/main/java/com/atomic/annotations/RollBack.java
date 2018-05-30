package com.atomic.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * 单库多表数据回滚注解
 * @author dreamyao
 * @version 1.0
 *          Created by dreamyao on 2017/5/28.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface RollBack {

    /**
     * 数据库名称
     * @return
     */
    String dbName() default "";

    /**
     * 数据库表名称
     * @return
     */
    String[] tableName() default {};

    /**
     * 开启或关闭
     * @return
     */
    boolean enabled() default true;
}
