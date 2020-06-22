package com.atomic.schema;

import com.atomic.config.GlobalConfig;
import com.atomic.util.ApplicationUtils;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * @author dreamyao
 * @title
 * @date 2018/6/5 下午6:12
 * @since 1.0.0
 */
public class AtomicBeanDefinitionParser implements BeanDefinitionParser {

    private static final Logger logger = LoggerFactory.getLogger(AtomicBeanDefinitionParser.class);
    private final Class<?> beanClass;

    public AtomicBeanDefinitionParser(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        return parse(element, parserContext, beanClass);
    }

    private BeanDefinition parse(Element element, ParserContext parserContext, Class<?> beanClass) {

        if (GlobalConfig.class.equals(beanClass)) {
            // 解析XML文件获取配置值
            Map<String, String> parameters = parseParameters(element);

            if (Boolean.FALSE.equals(CollectionUtils.isEmpty(parameters))) {
                GlobalConfig.projectName = parameters.get("projectName");
                GlobalConfig.runner = parameters.get("runner");
            }

            RootBeanDefinition beanDefinition = new RootBeanDefinition();
            beanDefinition.setBeanClass(ApplicationUtils.class);
            beanDefinition.setLazyInit(false);

            parserContext.registerBeanComponent(new BeanComponentDefinition(beanDefinition,
                    "applicationUtils"));
        }
        return null;
    }

    private Map<String, String> parseParameters(Element element) {
        Map<String, String> map = Maps.newHashMap();
        NamedNodeMap attributes = element.getAttributes();
        int length = attributes.getLength();
        for (int i = 0; i < length; i++) {
            Node item = attributes.item(i);
            map.put(item.getNodeName(), item.getNodeValue());
        }
        return map;
    }
}
