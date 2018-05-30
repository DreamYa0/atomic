package com.atomic.annotations;

import com.atomic.enums.CheckMessage;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Http接口响应消息返回类型
 * @author yangminhan
 * @version 1.0
 *          Created by yangminhan on 2017/6/18.
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface MessageType {

    CheckMessage checkMessage() default CheckMessage.NORM;
}
