package com.elektra.resumennomina.infrastructure.config.cache;

import com.elektra.resumennomina.infrastructure.config.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de cache en memoria.
 * Para produccion se podria usar Redis.
 */
@Configuration
@RequiredArgsConstructor
public class CacheConfig {

    private final AppProperties appProperties;

    public static final String COMPENSACION_NACIONAL = "compensacionNacional";
    public static final String COMPENSACION_POR_NEGOCIO = "compensacionPorNegocio";
    public static final String COMPENSACION_POR_PUESTO = "compensacionPorPuesto";
    public static final String ESTADISTICAS = "estadisticas";
    public static final String MAPA_CALOR = "mapaCalor";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                COMPENSACION_NACIONAL,
                COMPENSACION_POR_NEGOCIO,
                COMPENSACION_POR_PUESTO,
                ESTADISTICAS,
                MAPA_CALOR
        );
    }
}
