# ===========================================
# Dockerfile para Resumen Nomina API
# Ambiente: QA / PROD (AWS DocumentDB)
# ===========================================

# Base image
FROM local-docker-registry-base.artifactory.colaboracionsimba.net/openjdk:17

LABEL maintainer="COMPENSALIA"
LABEL application="resumen-nomina-api"
LABEL version="1.0.0"

# Configuración de timezone
ENV TZ=America/Mexico_City
ENV PROJECT_NAME="RESUMEN NOMINA API"

# Perfil de Spring Boot (qa por defecto)
ENV SPRING_PROFILES_ACTIVE=qa
ENV AWS_REGION=us-east-1

WORKDIR /opt

# Copiar JAR de la aplicación
COPY target/resumen-nomina-1.0.0-SNAPSHOT.jar /opt/resumen-nomina.jar

# Copiar certificado SSL para DocumentDB
COPY src/main/resources/documentdb-truststore.jks /opt/documentdb-truststore.jks

# Puerto de la aplicaciónZ
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Configuración JVM optimizada para contenedores
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -Djava.security.egd=file:/dev/./urandom \
               -Djavax.net.ssl.trustStore=/opt/documentdb-truststore.jks \
               -Djavax.net.ssl.trustStorePassword=changeit"

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /opt/resumen-nomina.jar"]
