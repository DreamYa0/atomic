package com.atomic.schema;

import com.atomic.util.ApplicationUtils;
import org.springframework.context.annotation.Bean;

/**
 * @author dreamyao
 * @title
 * @date 2021/10/15 7:20 下午
 * @since 1.0.0
 */
public class AtomicAutoTestConfiguration {

    @Bean
    public ApplicationUtils applicationUtils() {
        return new ApplicationUtils();
    }
}
