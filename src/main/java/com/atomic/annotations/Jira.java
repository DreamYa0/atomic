package com.atomic.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2018/05/30 10:48
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Jira {

    /**
     * jira名称
     */
    String jiraName() default "";

    /**
     * 标签
     */
    String labels() default "autotest";

    /**
     * bug受理人
     */
    String assignee() default "";

    /**
     * Issue 类型名称
     */
    String issueType() default "18";

    /**
     * bug等级
     */
    String priority() default "4";

    boolean enabled() default true;
}
