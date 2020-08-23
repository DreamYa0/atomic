package com.atomic.util;

import com.atomic.annotations.AnnotationUtils;
import com.atomic.exception.GetBeanException;
import com.atomic.param.util.ParamUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;


/**
 * @author dreamyao
 * @version 1.0 Created by dreamyao on 2017/5/29.
 */
@Component
public class ApplicationUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static Object getBean(Class<?> interfaceType) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        // 根据class获取bean的实例
        try {
            return applicationContext.getBean(interfaceType);
        } catch (Exception e) {
            try {
                // 接口代理，还可以配置 <aop:config proxy-target-class="true">
                return applicationContext.getBean(ParamUtils.lowerFirst(interfaceType.getSimpleName()));
            } catch (Exception e1) {
                try {
                    // 如果为代理类时获取他的目标类
                    Class<?> targetClass = AopUtils.getTargetClass(interfaceType);
                    return applicationContext.getBean(targetClass);
                } catch (Exception e2) {
                    // 通过接口获取实例，实现类有名称，例如 @Service("xxxService")
                    String beanName = AnnotationUtils.getBeanName(interfaceType);
                    if (beanName != null) {
                        return applicationContext.getBean(beanName);
                    } else {
                        // 在xml中配置的
                        // 如果 interfaceType 实现了多个接口就没法了，BaseTestCase<T>必须填写接口
                        // 后期可以通过 method 来一起定位到底是哪个接口
                        Class<?>[] classes = interfaceType.getInterfaces();
                        if (classes != null && classes.length > 0) {
                            return applicationContext.getBean(classes[0]);
                        }
                    }
                }
            }
        }
        throw new GetBeanException("bean not found : " + interfaceType.getName());
    }

    public static Object getBean(String beanName) {
        // 根据bean的名称获取bean的实例
        return applicationContext.getBean(beanName);
    }
}
