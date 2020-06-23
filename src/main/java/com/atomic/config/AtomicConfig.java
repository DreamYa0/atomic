package com.atomic.config;

import com.atomic.exception.ParameterException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @date  2018/05/30 10:48
 */
public class AtomicConfig {

    private static final Logger logger = LoggerFactory.getLogger(AtomicConfig.class);
    private static final AtomicConfig INSTANCE = new AtomicConfig();
    private Cache<String, Map<String, String>> centerConfigCache = CacheBuilder.newBuilder().build();

    private AtomicConfig() {
    }

    public static AtomicConfig newInstance() {
        return INSTANCE;
    }

    /**
     * 从 src/resource/properties/ 路径下读取配置文件
     * @param profile 环境名称
     * @return 配置信息
     */
    public Map<String, String> readPropertyConfig(String profile) {
        try {
            return centerConfigCache.get(profile,() -> {
                Map<String, String> config = Maps.newHashMap();
                try {

                    InputStream stream = AtomicConfig.class.getClassLoader().getResourceAsStream(
                            "zookeeper/" + profile + ".properties");
                    Properties properties = new Properties();
                    properties.load(stream);
                    Iterator<Object> iterator = properties.keySet().iterator();

                    while (iterator.hasNext()) {
                        Object key = iterator.next();
                        Object value = properties.get(key);
                        config.putIfAbsent(key.toString(), value.toString());
                    }
                } catch (IOException e) {
                    logger.error("读取配置文件失败！", e);
                }

                return config;
            });
        } catch (ExecutionException e) {
            logger.error("从缓存中获取对应环境配置文件失败！", e);
        }
        return Maps.newHashMap();
    }

    /**
     * 从配置信息中获取测试接口的域名或IP+Port
     * @return 域名或IP+Port
     */
    public String getHttpHost() {
        String httpHost = TesterConfig.getHttpHost();
        if (httpHost == null || "".equals(httpHost)) {
            throw new ParameterException("从配置中获取Http请求的域名或IP地址失败！");
        }
        return httpHost;
    }
}
