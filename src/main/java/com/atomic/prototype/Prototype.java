package com.atomic.prototype;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 具体原型
 * @Data 2018/05/30 10:48
 */
public class Prototype implements IPrototype {

    /**
     * 用于对象的深Clone
     * @return 对象的副本
     * @throws CloneNotSupportedException 对象不满足或不支持clone操作时
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Object obj = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            obj = objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
