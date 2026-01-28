package com.elektra.resumennomina;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableCaching
@EnableMongoRepositories(basePackages = "com.elektra.resumennomina.infrastructure.persistence.repository")
public class ResumenNominaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumenNominaApplication.class, args);
    }
}
