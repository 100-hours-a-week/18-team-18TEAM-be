package com.caro.bizkit.common.monitoring;

import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class QueryCountConfig {

    private final QueryCountInspector queryCountInspector;

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return properties -> properties.put(AvailableSettings.STATEMENT_INSPECTOR, queryCountInspector);
    }
}
