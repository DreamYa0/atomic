package com.atomic.tools.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.atomic.config.CenterConfig;
import com.atomic.config.GlobalConfig;
import com.atomic.exception.DubboServiceException;
import com.atomic.param.Constants;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import org.testng.Reporter;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author dreamyao
 * @Data 2018/05/30 10:48
 * @title
 */
@ThreadSafe
public class DubboServiceFactory {

    //以下为服务缓存相关代码 试用
    private static Map<String, Object> serviceCacheMap = Maps.newConcurrentMap();
    private final ApplicationConfig application;
    private final RegistryConfig registry;
    private final Cache<String, GenericService> genericServiceCache = CacheBuilder.newBuilder().build();
    private final Cache<String, Object> serviceCache = CacheBuilder.newBuilder().build();
    private volatile String host = "192.168.143.34:2181";
    private volatile String profile;

    public DubboServiceFactory(String env) {
        Map<String, String> maps = CenterConfig.newInstance().readPropertyConfig(env);
        host = maps.get(Constants.ZOOKEEPER);
        application = new ApplicationConfig();
        application.setName("atomic_config");
        // 连接注册中心配置
        registry = new RegistryConfig();
        registry.setAddress("zookeeper://" + host);
        registry.setUsername("atomic");
    }

    public DubboServiceFactory() {
        GlobalConfig.load();//加载环境配置文件
        profile = GlobalConfig.getProfile();
        Map<String, String> maps = CenterConfig.newInstance().readPropertyConfig(profile);
        host = maps.get(Constants.ZOOKEEPER);
        application = new ApplicationConfig();
        application.setName("atomic_config");
        // 连接注册中心配置
        registry = new RegistryConfig();
        registry.setAddress("zookeeper://" + host);
        registry.setUsername("atomic");
    }

    /**
     * 从缓存服务中获取服务
     * @param clazz
     * @param testMode
     * @param version
     * @param <T>
     * @return
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    private static <T> T getCacheService(Class<? extends T> clazz, String testMode, String... version) {
        String key = clazz.getName() + "_" + testMode;
        if (version != null && version.length > 0) {
            key = key + "_" + version[0];
        }
        return (T) serviceCacheMap.get(key);
    }

    /**
     * 缓存服务
     * @param o
     * @param clazz
     * @param testMode
     * @param version
     */
    @Deprecated
    private static void cacheService(Object o, Class<?> clazz, String testMode, String... version) {
        String key = clazz.getName() + "_" + testMode;
        if (version != null && version.length > 0) {
            key = key + "_" + version[0];
        }
        if (serviceCacheMap.containsKey(key)) {
            serviceCacheMap.remove(key);
        }
        serviceCacheMap.put(key, o);
    }

    /**
     * 获取远程服务代理
     * @param clazz
     * @param version
     * @param <T>
     * @return
     */
    public <T> T getService(Class<? extends T> clazz, String... version) {
        //为获取覆盖率做全局配置,强制指定请求服务地址
        try {
            if (GlobalConfig.hostDomain != null) {
                String url = "META_INF.dubbo://" + GlobalConfig.hostDomain + "/"
                        + clazz.getName();
                T t = getServiceByUrl(clazz, url, version);
                checkService(t, clazz);
                return t;
                //此处获取url的方式不够稳定 可能有风险
            }
        } catch (Exception e) {
            throw new DubboServiceException("获取dubbo服务异常！", e);
        }
        return getService(clazz, profile, version);
    }

    /**
     * 先从缓存中获取服务，如果本地缓存没有此服务则从远程注册中心获取
     * @param clazz
     * @param profile
     * @param version
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> T getService(Class<? extends T> clazz, String profile, String... version) {
        String key = clazz.getName() + "_" + profile;
        if (version != null && version.length > 0) {
            key = key + "_" + version[0];
        }
        Object object;
        try {
            object = serviceCache.get(key, () -> getRemoteService(clazz, version));
        } catch (ExecutionException e) {
            Reporter.log("[DubboServiceFactory#getService()]:{} ---> 获取远程服务失败！");
            throw new DubboServiceException("获取dubbo服务异常！", e);
        }
        return (T) object;
    }

    /**
     * 从远程注册中心获取服务
     * @param clazz
     * @param version
     * @param <T>
     * @return
     */
    private <T> T getRemoteService(Class<? extends T> clazz, String... version) {
        // 引用远程服务
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setRegistry(registry);
        if (version != null && version.length != 0) {
            reference.setVersion(version[0]);
        }
        reference.setTimeout(10000);
        reference.setInterface(clazz);
        return reference.get();
    }

    /**
     * 获取服务的泛化调用
     * @param clazz
     * @return
     */
    public GenericService getGenericService(Class<?> clazz) {
        String interfaceName = clazz.getSimpleName();
        GenericService genericService;
        try {
            genericService = genericServiceCache.get(interfaceName, () -> {
                // 引用远程服务
                ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
                reference.setApplication(application);
                reference.setRegistry(registry); // 多个注册中心可以用setRegistries()
                reference.setInterface(clazz);
                reference.setGeneric(true);
                reference.setTimeout(60000 * 2);
                return reference.get();
            });
        } catch (ExecutionException e) {
            Reporter.log("[DubboServiceFactory#getGenericService()]:{} ---> 获取远程服务失败！");
            throw new DubboServiceException("获取dubbo服务异常！", e);
        }
        return genericService;
    }

    /**
     * 获取服务的泛化调用
     * @param interfaceName
     * @return
     */
    public GenericService getGenericService(String interfaceName) {
        GenericService genericService;
        try {
            // 引用远程服务
            genericService = genericServiceCache.get(interfaceName, () -> {
                ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
                reference.setGeneric(true);
                reference.setApplication(application);
                reference.setRegistry(registry); // 多个注册中心可以用setRegistries()
                reference.setInterface(interfaceName);
                reference.setTimeout(60000 * 2);
                return reference.get();
            });
        } catch (ExecutionException e) {
            Reporter.log("[DubboServiceFactory#getGenericService()]:{} ---> 获取远程服务失败！");
            throw new DubboServiceException("获取dubbo服务异常！", e);
        }
        return genericService;
    }

    /**
     * 获取点对点直连的service
     * @return
     */
    private <T> T getServiceByUrl(Class<? extends T> clazz, String url, String... version) {
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setUrl(url);
        reference.setInterface(clazz);
        reference.setTimeout(10000);
        if (version != null && version.length != 0) {
            reference.setVersion(version[0]);
        }
        return reference.get();
    }

    /**
     * 检测获取的服务是否存在
     * @param t
     * @param clazz
     * @param <T>
     * @throws Exception
     */
    private <T> void checkService(T t, Class<? extends T> clazz) throws Exception {
        Method method = clazz.getMethods()[0];
        Class[] parameterTypes = method.getParameterTypes();
        Object[] params = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Object o1 = parameterTypes[i].newInstance();
            params[i] = o1;
        }
        method.invoke(t, params);
    }

    public String getHost() {
        return host;
    }
}
