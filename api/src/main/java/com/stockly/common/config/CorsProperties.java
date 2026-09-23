package com.stockly.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stockly.cors")
public record CorsProperties(String origins) {
}
