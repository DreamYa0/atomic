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

    /**
     * 把source覆盖到target里面去
     * @param target
     * @param source
     */
    @SuppressWarnings("unchecked")
    public static void mergeMap(Map target, Map source) {
        if (source == null || target == null)
            return;
        for (Object key : source.keySet()) {
            target.put(key, source.get(key));
        }
    }

    /**
     * 获取Key对应Value，并转换成Integer
     * @param map
     * @param key
     * @return
     */
    private static Integer getInt(Map<?, ?> map, Object key) {
        try {
            Object val = map.get(key);
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
            return Integer.valueOf(val.toString());
        } catch (Exception e) {
            Reporter.log("[MapUtil#getInt()]:{数据转化异常！}");
        }
        return null;
    }

    /**
     * 获取Key对应Value，并转换成Integer
     * @param map
     * @param key
     * @param defaultValue value不存在或者转换失败时，返回该默认值
     * @return
     */
    public static Integer getInt(Map<?, ?> map, Object key, Integer defaultValue) {
        Integer ret = getInt(map, key);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }

    /**
     * 获取Key对应Value，并转换成String
     * @param map
     * @param key
     * @return
     */
    private static String getString(Map<?, ?> map, Object key) {
        if (map.get(key) == null) {
            return null;
        } else {
            return map.get(key).toString();
        }
    }

    /**
     * 获取Key对应Value，并转换成String
     * @param map
     * @param key
     * @param defaultValue value不存在或者转换失败时，返回该默认值
     * @return
     */
    public static String getString(Map<?, ?> map, Object key, String defaultValue) {
        String ret = getString(map, key);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }

    /**
     * 获取Key对应Value，并转换成BigDecimal
     * @param map
     * @param key
     * @return
     */
    public static BigDecimal getBigDecimal(Map<?, ?> map, Object key) {
        try {
            return new BigDecimal(map.get(key).toString());
        } catch (Exception e) {
            Reporter.log("[MapUtil#getBigDecimal()]:{数据转化异常！}");
        }
        return null;
    }

    /**
     * 获取Key对应Value，并转换成BigDecimal
     * @param map
     * @param key
     * @param defaultValue value不存在或者转换失败时，返回该默认值
     * @return
     */
    public static BigDecimal getBigDecimal(Map<?, ?> map, Object key, BigDecimal defaultValue) {
        BigDecimal ret = getBigDecimal(map, key);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }

    /**
     * 获取Key对应Value，并转换成Map
     * @param map
     * @param key
     * @return
     */
    public static Map<?, ?> getMap(Map<?, ?> map, Object key) {
        try {
            return (Map<?, ?>) map.get(key);
        } catch (Exception e) {
            Reporter.log("[MapUtil#getMap()]:{数据转化异常！}");
        }

        return null;
    }

    /**
     * 获取Key对应Value，并转换成Map
     * @param map
     * @param key
     * @param defaultValue value不存在或者转换失败时，返回该默认值
     * @return
     */
    public static Map<?, ?> getMap(Map<?, ?> map, Object key, Map<?, ?> defaultValue) {
        Map<?, ?> ret = getMap(map, key);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }
}
