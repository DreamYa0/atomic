package com.atomic.schema;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

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
@Import(AtomicAutoTestConfiguration.class)
public @interface EnableAtomic {

}
