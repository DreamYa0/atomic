package com.atomic.schema;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dreamyao
 * @title 开启Atomic自动化测试
 * @date 2018/6/5 下午7:30
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@Import(AtomicAutoTestRegistrar.class)
public @interface EnableAtomic {

    /**
     * 项目名称
     * @return 项目名称
     */
    String projectName();

    /**
     * 测试运行人
     * @return 测试运行人
     */
    String runner();
}
