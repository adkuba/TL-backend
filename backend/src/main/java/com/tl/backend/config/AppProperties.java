package com.tl.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tl.config")
public class AppProperties {
    private String apiDomain;
    private String domain;
    private String stripeKey;
    private String googleRecaptcha;
}
