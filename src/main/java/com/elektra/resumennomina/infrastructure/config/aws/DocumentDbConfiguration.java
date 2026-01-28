package com.elektra.resumennomina.infrastructure.config.aws;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * Configuración de MongoDB/DocumentDB para ambientes QA y PROD.
 * Lee credenciales desde AWS Systems Manager Parameter Store.
 */
@Slf4j
@Configuration
@Profile({"qa", "prod"})
public class DocumentDbConfiguration {

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${spring.data.mongodb.parameter}")
    private String parameter;

    private final SystemsManagerConfiguration ssmConfig;

    public DocumentDbConfiguration(SystemsManagerConfiguration ssmConfig) {
        this.ssmConfig = ssmConfig;
    }

    @Bean
    public MongoMappingContext mongoMappingContext() {
        MongoMappingContext context = new MongoMappingContext();
        context.setAutoIndexCreation(true);
        return context;
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoMappingContext mongoMappingContext) {
        log.info("Configurando conexión a DocumentDB para ambiente QA/PROD");

        // Leer credenciales desde SSM Parameter Store
        JSONObject json = ssmConfig.readParameter(parameter);

        String mongoUri = json.getString("documentdb.uri");
        String trustStorePath = json.getString("documentdb.ssl.truststore.path");
        String trustStorePassword = json.getString("documentdb.ssl.truststore.password");

        log.info("Truststore path: {}", trustStorePath);

        try {
            // Crear SSLContext específico para MongoDB (no afecta otras conexiones SSL)
            SSLContext sslContext = createSSLContext(trustStorePath, trustStorePassword);

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(mongoUri))
                    .applyToSslSettings(builder -> {
                        builder.enabled(true)
                               .invalidHostNameAllowed(true)
                               .context(sslContext);
                    })
                    .build();

            MongoClient mongoClient = MongoClients.create(settings);
            SimpleMongoClientDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(mongoClient, database);

            MappingMongoConverter converter = new MappingMongoConverter(
                    new DefaultDbRefResolver(factory), mongoMappingContext);

            log.info("Conexión a DocumentDB configurada exitosamente");
            return new MongoTemplate(factory, converter);

        } catch (Exception e) {
            log.error("Error configurando SSL para DocumentDB: {}", e.getMessage(), e);
            throw new RuntimeException("Error configurando SSL para DocumentDB", e);
        }
    }

    /**
     * Crea un SSLContext con el truststore de DocumentDB.
     * Este SSLContext se usa SOLO para MongoDB, no afecta otras conexiones SSL del sistema.
     */
    private SSLContext createSSLContext(String trustStorePath, String password) throws Exception {
        log.info("Creando SSLContext con truststore");

        // Determinar la ruta completa del truststore
        Path truststoreFile;
        if (Paths.get(trustStorePath).isAbsolute()) {
            truststoreFile = Paths.get(trustStorePath);
        } else {
            // Ruta relativa desde el directorio de trabajo actual
            truststoreFile = Paths.get(System.getProperty("user.dir"), trustStorePath);
        }

        log.info("Ruta completa del truststore: {}", truststoreFile.toAbsolutePath());

        if (!Files.exists(truststoreFile)) {
            throw new RuntimeException("Truststore no encontrado en: " + truststoreFile.toAbsolutePath());
        }

        // Cargar truststore
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (InputStream is = new FileInputStream(truststoreFile.toFile())) {
            trustStore.load(is, password.toCharArray());
        }

        // Inicializar TrustManagerFactory
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // Crear SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

        log.info("SSLContext creado exitosamente");
        return sslContext;
    }
}
