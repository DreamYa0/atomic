package com.atomic.util;


import org.testng.Reporter;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/29.
 */
public final class MapUtils {

    private MapUtils() {
    }

    @SuppressWarnings("unchecked")
    public static void mergeMap(Map target, Map source) {
        // 把source覆盖到target里面去
        if (source == null || target == null)
            return;
        for (Object key : source.keySet()) {
            target.put(key, source.get(key));
        }
    }

    private static Integer getInt(Map<?, ?> map, Object key) {
        // 获取Key对应Value，并转换成Integer
        try {
            Object val = map.get(key);
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
            return Integer.valueOf(val.toString());
        } catch (Exception e) {
            Reporter.log("数据转化异常！");
        }
        return null;
    }

    public static Integer getInt(Map<?, ?> map, Object key, Integer defaultValue) {
        // 获取Key对应Value，并转换成Integer
        Integer ret = getInt(map, key);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }

    private static String getString(Map<?, ?> map, Object key) {
        // 获取Key对应Value，并转换成String
        if (map.get(key) == null) {
            return null;
        } else {
            return map.get(key).toString();
        }
    }

    public static String getString(Map<?, ?> map, Object key, String defaultValue) {
        // 获取Key对应Value，并转换成String
        String ret = getString(map, key);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }

    public static BigDecimal getBigDecimal(Map<?, ?> map, Object key) {
        // 获取Key对应Value，并转换成BigDecimal
        try {
            return new BigDecimal(map.get(key).toString());
        } catch (Exception e) {
            Reporter.log("数据转化异常！");
        }
        return null;
    }

    public static BigDecimal getBigDecimal(Map<?, ?> map, Object key, BigDecimal defaultValue) {
        // 获取Key对应Value，并转换成BigDecimal
        BigDecimal ret = getBigDecimal(map, key);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }

    public static Map<?, ?> getMap(Map<?, ?> map, Object key) {
        // 获取Key对应Value，并转换成Map
        try {
            return (Map<?, ?>) map.get(key);
        } catch (Exception e) {
            Reporter.log("数据转化异常！");
        }

        return null;
    }

    public static Map<?, ?> getMap(Map<?, ?> map, Object key, Map<?, ?> defaultValue) {
        // 获取Key对应Value，并转换成Map
        Map<?, ?> ret = getMap(map, key);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }
}
