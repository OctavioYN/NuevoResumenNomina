package com.elektra.resumennomina.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String name;
    private String version;
    private CorsProperties cors = new CorsProperties();
    private CacheProperties cache = new CacheProperties();

    @Data
    public static class CorsProperties {
        private String allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
        private boolean allowCredentials;
    }

    @Data
    public static class CacheProperties {
        private boolean enabled = true;
        private int ttlSeconds = 300;
    }
}
