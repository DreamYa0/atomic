package com.atomic.config;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import java.util.Map;

/**
 * @author dreamyao
 * @title
 * @Data 2017/7/3 下午3:41
 * @since 1.0.0
 */
public class AutoTestScope implements Scope {

    private Map<String, Object> objMap = Maps.newHashMap();

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Object object = objMap.get(name);
        if (object == null) {
            object = objectFactory.getObject();
            objMap.put(name, object);
        }

        return object;
    }

    @Override
    public Object remove(String name) {
        return objMap.remove(name);
    }

    @Override
    public void registerDestructionCallback(String s, Runnable runnable) {

    }

    @Override
    public Object resolveContextualObject(String s) {
        return null;
    }

    @Override
    public String getConversationId() {
        return null;
    }
}
