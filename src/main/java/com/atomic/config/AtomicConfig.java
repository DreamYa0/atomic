package com.atomic.config;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.setting.dialect.Props;

/**
 * @author dreamyao
 * @title
 * @date 16/5/23 下午7:55
 * @since 1.0.0
 */
public class AtomicConfig {

    private static final String TEST_CONFIG_FILE_PATH = "test.properties";
    private volatile static Props props;

    static {
        load();
    }

    private static void load() {
        if (props == null) {
            synchronized (AtomicConfig.class) {
                if (props == null) {
                    props = Props.getProp(TEST_CONFIG_FILE_PATH, CharsetUtil.UTF_8);
                }
            }
        }
    }

    public static String getStr(String key) {
        return props.getStr(key);
    }

    public static boolean containsKey(String key) {
        return props.containsKey(key);
    }
}
