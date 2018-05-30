package com.atomic.annotations;

import com.atomic.config.TestMethodMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mode {
    TestMethodMode value() default TestMethodMode.NORMAL;
}
