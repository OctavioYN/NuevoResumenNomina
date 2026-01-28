package com.elektra.resumennomina.infrastructure.config.aws;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

/**
 * Configuración para leer parámetros de AWS Systems Manager Parameter Store.
 * Usado para obtener credenciales de DocumentDB desde Secrets Manager.
 */
@Slf4j
@Component
public class SystemsManagerConfiguration {

    /**
     * Lee un parámetro de AWS SSM Parameter Store.
     *
     * @param parameterName nombre del parámetro (ej: /aws/reference/secretsmanager/qa/resumennomina/database/documentdb)
     * @return JSONObject con el contenido del parámetro
     */
    public JSONObject readParameter(String parameterName) {
        try {
            String region = System.getenv("AWS_REGION");
            if (region == null || region.isEmpty()) {
                region = "us-east-1";
            }

            log.info("Leyendo parámetro SSM: {}", parameterName);
            log.info("Usando región AWS: {}", region);

            try (SsmClient ssmClient = SsmClient.builder()
                    .region(Region.of(region))
                    .build()) {

                GetParameterRequest request = GetParameterRequest.builder()
                        .name(parameterName)
                        .withDecryption(true)
                        .build();

                GetParameterResponse response = ssmClient.getParameter(request);
                String parameterValue = response.parameter().value();

                log.info("Parámetro SSM leído correctamente");

                return new JSONObject(parameterValue);
            }

        } catch (Exception e) {
            log.error("Error leyendo parámetro SSM: {}", e.getMessage());
            throw new RuntimeException("No se pudo leer parámetro: " + parameterName, e);
        }
    }
}
