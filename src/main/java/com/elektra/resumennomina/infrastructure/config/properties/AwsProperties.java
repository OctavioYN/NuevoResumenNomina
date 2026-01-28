package com.elektra.resumennomina.infrastructure.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aws.secrets")
public class AwsProperties {

    private boolean enabled = false;
    private String region = "us-east-1";
    private String mongodbSecretName;
}
