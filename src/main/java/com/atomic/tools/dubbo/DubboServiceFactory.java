package com.atomic.tools.dubbo;

import cn.hutool.core.util.StrUtil;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.atomic.config.ConfigConstants;
import com.atomic.config.AtomicConfig;
import com.atomic.exception.DubboServiceException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.testng.Reporter;

import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

/**
 * @author dreamyao
 * @date
 * @title
 */
@ThreadSafe
public class DubboServiceFactory {

    private final ApplicationConfig application;
    private final RegistryConfig registry;
    private final Cache<String, GenericService> genericServiceCache = CacheBuilder.newBuilder().build();
    private final Cache<String, Object> serviceCache = CacheBuilder.newBuilder().build();
    private final String host;

    public DubboServiceFactory() {
        host = AtomicConfig.getStr(ConfigConstants.DUBBO_ZOOKEEPER);
        application = new ApplicationConfig();
        application.setName("atomic_config");
        // 连接注册中心配置
        registry = new RegistryConfig();
        registry.setAddress("zookeeper://" + host);
        registry.setUsername("atomic");
        registry.setProtocol("dubbo");
        registry.setRegister(false);
    }

    /**
     * 获取远程服务代理
     * @param clazz   clazz
     * @param version 服务版本
     * @param group   服务组
     * @param <T>     类型
     * @return 实例
     */
    public <T> T getService(Class<? extends T> clazz, String version, String group) {
        //为获取覆盖率做全局配置,强制指定请求服务地址
        try {
            final String providerHost = AtomicConfig.getStr(ConfigConstants.DUBBO_PROVIDER_HOST);
            if (StrUtil.isNotBlank(providerHost)) {
                String url = "dubbo://" + providerHost + "/" + clazz.getName();
                T t = getServiceByUrl(clazz, url, version, group);
                checkService(t, clazz);
                return t;
                //此处获取url的方式不够稳定 可能有风险
            }
        } catch (Exception e) {
            throw new DubboServiceException("获取dubbo服务异常！", e);
        }
        return doGetService(clazz, version, group);
    }

    @SuppressWarnings("unchecked")
    private <T> T doGetService(Class<? extends T> clazz, String version, String group) {
        // 先从缓存中获取服务，如果本地缓存没有此服务则从远程注册中心获取
        String key = clazz.getName() + "_" + "_" + version;
        Object object;
        try {
            object = serviceCache.get(key, () -> getRemoteService(clazz, version,
                    group));
        } catch (ExecutionException e) {
            Reporter.log("获取远程服务失败！");
            throw new DubboServiceException("获取dubbo服务异常！", e);
        }
        return (T) object;
    }

    private <T> T getRemoteService(Class<? extends T> clazz, String version, String group) {
        // 引用远程服务，从远程注册中心获取服务
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setRegistry(registry);
        if (StrUtil.isNotBlank(version)) {
            reference.setVersion(version);
        }
        if (StrUtil.isNotBlank(group)) {
            reference.setGroup(group);
        }
        reference.setTimeout(10000);
        reference.setInterface(clazz);
        reference.setProtocol("dubbo");
        reference.setLoadbalance("roundrobin");
        return reference.get();
    }

    public GenericService getGenericService(Class<?> clazz) {
        // 获取服务的泛化调用
        String interfaceName = clazz.getSimpleName();
        GenericService genericService;
        try {
            genericService = genericServiceCache.get(interfaceName, () -> {
                // 引用远程服务
                ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
                reference.setApplication(application);
                // 多个注册中心可以用setRegistries()
                reference.setRegistry(registry);
                reference.setInterface(clazz);
                reference.setGeneric(true);
                reference.setTimeout(60000 * 2);
                return reference.get();
            });
        } catch (ExecutionException e) {
            Reporter.log("获取远程服务失败！");
            throw new DubboServiceException("获取dubbo服务异常！", e);
        }
        return genericService;
    }

    public GenericService getGenericService(String interfaceName) {
        // 获取服务的泛化调用
        GenericService genericService;
        try {
            // 引用远程服务
            genericService = genericServiceCache.get(interfaceName, () -> {
                ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
                reference.setGeneric(true);
                reference.setApplication(application);
                // 多个注册中心可以用setRegistries()
                reference.setRegistry(registry);
                reference.setInterface(interfaceName);
                reference.setTimeout(60000 * 2);
                return reference.get();
            });
        } catch (ExecutionException e) {
            Reporter.log("获取远程服务失败！");
            throw new DubboServiceException("获取dubbo服务异常！", e);
        }
        return genericService;
    }

    private <T> T getServiceByUrl(Class<? extends T> clazz, String url, String version, String group) {
        // 获取点对点直连的service
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setApplication(application);
        reference.setUrl(url);
        reference.setInterface(clazz);
        reference.setTimeout(10000);
        if (StrUtil.isNotBlank(version)) {
            reference.setVersion(version);
        }
        if (StrUtil.isNotBlank(group)) {
            reference.setGroup(group);
        }
        return reference.get();
    }

    private <T> void checkService(T t, Class<? extends T> clazz) throws Exception {
        // 检测获取的服务是否存在
        Method method = clazz.getMethods()[0];
        Class<?>[] parameterTypes = method.getParameterTypes();
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
