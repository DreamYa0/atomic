package com.atomic.tools.dubbo;

import com.atomic.exception.DubboServiceException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.testng.Reporter;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.ExecutionException;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title
 * @Date 2017/8/28 21:20
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
        try {
            /*ConfigCenterConfigurer configurer;
            try {
                configurer = (ConfigCenterConfigurer) getBean(ConfigCenterConfigurer.class);
            } catch (Exception e) {
                configurer = (ConfigCenterConfigurer) getBean(CustomPropertyConfigurer.class);
            }
            if (configurer != null) {
                profile = configurer.getProfile();
                String model = configurer.getApplication();
                String version = configurer.getVersion();
                // 确定配置中心的坐标(module,profile,version)
                ConfigInfo configInfo = new ConfigInfo(model, profile, version);
                // 获取运行时配置
                RuntimeConfig conf = RuntimeConfig.getInstance(configInfo);
                Map<String, CompositedConfigItem> configItemMap = conf.getCurrentConfigMap();
                configItemMap.keySet().forEach(key -> {
                    if (key.contains("dubbo") && key.contains("registry") && key.contains("address")) {
                        CompositedConfigItem configItem = conf.getCurrentConfig(key);
                        dubboRegistry[0] = configItem.getCurrentValue();
                    }
                });
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        application = new ApplicationConfig();
        application.setName("Zeratul_Config");
        // 连接注册中心配置
        registry = new RegistryConfig();
        registry.setAddress(dubboRegistry[0]);
        registry.setUsername("Zeratul");
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
