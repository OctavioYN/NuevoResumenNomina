package com.elektra.resumennomina.infrastructure.config.openapi;

import com.elektra.resumennomina.infrastructure.config.properties.AppProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    private final AppProperties appProperties;
    private final Environment environment;

    @Bean
    public OpenAPI customOpenAPI() {
        String activeProfile = String.join(", ", environment.getActiveProfiles());

        return new OpenAPI()
                .info(new Info()
                        .title(appProperties.getName())
                        .version(appProperties.getVersion())
                        .description("API para el sistema de Resumen de Nomina de Grupo Elektra. " +
                                "Ambiente actual: " + activeProfile)
                        .contact(new Contact()
                                .name("Grupo Elektra - Inteligencia de Datos")
                                .email("inteligencia.datos@elektra.com"))
                        .license(new License()
                                .name("Propietario")
                                .url("https://www.elektra.com")))
                .servers(getServers());
    }

    private List<Server> getServers() {
        return List.of(
                new Server()
                        .url("/")
                        .description("Servidor actual")
        );
    }
}
