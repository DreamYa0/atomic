package com.atomic.schema;

import com.atomic.config.TesterConfig;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author dreamyao
 * @title
 * @date 2018/6/5 下午6:12
 * @since 1.0.0
 */
public class AtomicNamespaceHandler  extends NamespaceHandlerSupport {

    static {
        Version.checkDuplicate(AtomicNamespaceHandler.class);
    }

    public void init() {
        registerBeanDefinitionParser("atomic",
                new AtomicBeanDefinitionParser(TesterConfig.class));
    }
}
