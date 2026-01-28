package com.elektra.resumennomina.infrastructure.config.aws;

import com.elektra.resumennomina.infrastructure.config.properties.AwsProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuracion para obtener secretos de AWS Secrets Manager.
 * Solo se activa cuando aws.secrets.enabled=true (ambientes QA y PROD)
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "aws.secrets.enabled", havingValue = "true")
public class AwsSecretsManagerConfig {

    private final AwsProperties awsProperties;
    private final ConfigurableEnvironment environment;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadSecrets() {
        log.info("Cargando secretos desde AWS Secrets Manager - Region: {}", awsProperties.getRegion());

        try (SecretsManagerClient client = createSecretsManagerClient()) {
            loadMongoDbSecret(client);
        } catch (Exception e) {
            log.error("Error al cargar secretos de AWS: {}", e.getMessage());
            throw new RuntimeException("No se pudieron cargar los secretos de AWS", e);
        }
    }

    private SecretsManagerClient createSecretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .build();
    }

    private void loadMongoDbSecret(SecretsManagerClient client) {
        String secretName = awsProperties.getMongodbSecretName();

        if (secretName == null || secretName.isEmpty()) {
            log.warn("No se configuro el nombre del secreto de MongoDB");
            return;
        }

        log.info("Obteniendo secreto de MongoDB: {}", secretName);

        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse response = client.getSecretValue(request);
        String secretString = response.secretString();

        try {
            JsonNode secretJson = objectMapper.readTree(secretString);
            String mongoUri = buildMongoUri(secretJson);

            Map<String, Object> secretProperties = new HashMap<>();
            secretProperties.put("spring.data.mongodb.uri", mongoUri);

            environment.getPropertySources().addFirst(
                    new MapPropertySource("aws-secrets", secretProperties)
            );

            log.info("Secreto de MongoDB cargado exitosamente");
        } catch (Exception e) {
            log.error("Error al parsear el secreto de MongoDB: {}", e.getMessage());
            throw new RuntimeException("Error al parsear secreto de MongoDB", e);
        }
    }

    /**
     * Construye la URI de MongoDB a partir del secreto de AWS.
     * El secreto debe tener la estructura:
     * {
     *   "username": "...",
     *   "password": "...",
     *   "host": "...",
     *   "port": "27017",
     *   "database": "...",
     *   "options": "retryWrites=true&w=majority" (opcional)
     * }
     * O simplemente:
     * {
     *   "uri": "mongodb+srv://..."
     * }
     */
    private String buildMongoUri(JsonNode secretJson) {
        // Si el secreto contiene la URI completa, usarla directamente
        if (secretJson.has("uri")) {
            return secretJson.get("uri").asText();
        }

        // Construir la URI a partir de los componentes
        String username = secretJson.get("username").asText();
        String password = secretJson.get("password").asText();
        String host = secretJson.get("host").asText();
        String port = secretJson.has("port") ? secretJson.get("port").asText() : "27017";
        String database = secretJson.get("database").asText();
        String options = secretJson.has("options") ? secretJson.get("options").asText() : "";

        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append("mongodb://")
                .append(username).append(":").append(password)
                .append("@").append(host).append(":").append(port)
                .append("/").append(database);

        if (!options.isEmpty()) {
            uriBuilder.append("?").append(options);
        }

        return uriBuilder.toString();
    }
}
