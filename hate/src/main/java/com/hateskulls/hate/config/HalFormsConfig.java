package com.hateskulls.hate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;

@Configuration
@EnableHypermediaSupport(type = { 
    HypermediaType.HAL, 
    HypermediaType.HAL_FORMS 
})
public class HalFormsConfig {
    // HAL-FORMS support is automatically configured
}
