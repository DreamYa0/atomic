package com.atomic.tools.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.atomic.config.CenterConfig;
import com.atomic.config.GlobalConfig;
import com.atomic.exception.DubboServiceException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.testng.Reporter;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ExecutionException;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Data 2017/8/28 21:20
 */
@ThreadSafe
public class DubboServiceStream {

    private static final DubboServiceStream INSTANCE = new DubboServiceStream();
    private final ApplicationConfig application;
    private final RegistryConfig registry;
    private final Cache<String, Object> serviceCache = CacheBuilder.newBuilder().build();
    private volatile String profile;

    private DubboServiceStream() {
        final String[] dubboRegistry = {"zookeeper://192.168.143.34:2181"};
        GlobalConfig.load();
        profile = GlobalConfig.getProfile();
        CenterConfig.newInstance().readPropertyConfig(profile);
        application = new ApplicationConfig();
        application.setName("atomic_Config");
        // 连接注册中心配置
        registry = new RegistryConfig();
        registry.setAddress(dubboRegistry[0]);
        registry.setUsername("atomic");
    }

    public static DubboServiceStream dubboServiceFactory() {
        return INSTANCE;
    }

    /**
     * 获取远程服务代理
     * @param clazz
     * @param version
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz, String... version) {
        String key = clazz.getName() + "_" + profile;
        if (version != null && version.length == 1) {
            key = key + "_" + version[0];
        }
        Object object;
        try {
            object = serviceCache.get(key, () -> {
                assert version != null;
                return getRemoteService(clazz, version);
            });
        } catch (ExecutionException e) {
            Reporter.log("[DubboServiceFactory#getService()]:{} ---> 获取远程服务失败！");
            throw new DubboServiceException("获取dubbo服务异常！", e);
        }
        return (T) object;
    }

    private <T> T getRemoteService(Class<T> clazz, String... version) {
        // 引用远程服务
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setRegistry(registry);
        if (version.length == 1) {
            reference.setVersion(version[0]);
        }
        reference.setTimeout(10000);
        reference.setInterface(clazz);
        return reference.get();
    }
}
