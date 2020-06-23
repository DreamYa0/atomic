package com.atomic.tools.rollback;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * 多库多表数据回滚注解
 * @author dreamyao
 * @version 1.0
 * @date 2018/05/30 10:48
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface RollBackAll {

    /**
     * 多表多库数据回滚
     * @return
     */
    String[] dbAndTable() default {};

    /**
     * 开启或关闭
     * @return
     */
    boolean enabled() default true;
}
