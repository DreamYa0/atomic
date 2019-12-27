package com.atomic.prototype;

import java.io.Serializable;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 抽象原型
 * @Data 2018/05/30 10:48
 */
public interface IPrototype extends Cloneable, Serializable {

    /**
     * 用于对象的深Clone
     * @return 对象的副本
     * @throws CloneNotSupportedException 对象不满足或不支持clone操作时
     */
    Object clone() throws CloneNotSupportedException;
}
