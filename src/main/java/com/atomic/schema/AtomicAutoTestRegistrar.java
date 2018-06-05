package com.atomic.schema;

import com.atomic.config.GlobalConfig;
import com.atomic.util.ApplicationUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author dreamyao
 * @title SpringBoot注解风格扩展IOC
 * @date 2018/6/5 下午7:33
 * @since 1.0.0
 */
public class AtomicAutoTestRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {

        Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(EnableAtomic.class.getName(), false);
        GlobalConfig.projectName = attributes.get("projectName").toString();
        GlobalConfig.runner = attributes.get("runner").toString();

        BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(ApplicationUtils.class);
        beanDefinitionRegistry.registerBeanDefinition("applicationUtils", definition.getBeanDefinition());
    }
}