package com.kulturman.irembotest.infrastructure.configuration;

import com.kulturman.irembotest.domain.ports.TenancyProvider;
import com.kulturman.irembotest.infrastructure.configuration.security.SecurityTenancyProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TenancyConfiguration {

    @Bean
    public TenancyProvider tenancyProvider() {
        return new SecurityTenancyProvider();
    }
}
