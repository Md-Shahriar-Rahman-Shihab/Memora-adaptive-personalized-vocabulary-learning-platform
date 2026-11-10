package com.memora.security;

import com.memora.modules.partner.security.PartnerPlacementInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Web MVC configuration registering interceptors for authorization enforcement.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final PartnerPlacementInterceptor partnerPlacementInterceptor;

    public WebMvcConfig(PartnerPlacementInterceptor partnerPlacementInterceptor) {
        this.partnerPlacementInterceptor = partnerPlacementInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(partnerPlacementInterceptor)
                .addPathPatterns("/api/v1/partners", "/api/v1/partners/**");
    }
}

