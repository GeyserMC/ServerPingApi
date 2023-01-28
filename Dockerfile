# Adapted from: https://github.com/PaperMC/bibliothek/blob/063cc014193b6ea160b3cabc7b93f70009e1fb2a/Dockerfile
ARG JAVA_VERSION=17
ARG JVM_FLAVOR=hotspot

FROM eclipse-temurin:${JAVA_VERSION}-jdk-alpine AS builder
WORKDIR /build

COPY ./ ./
RUN ./gradlew clean buildForDocker --no-daemon


ARG JAVA_VERSION
ARG JVM_FLAVOR

FROM eclipse-temurin:${JAVA_VERSION}-alpine
WORKDIR /app

RUN groupadd --system pingapi \
    && useradd --system pingapi --gid pingapi \
    && chown -R pingapi:pingapi /app
USER pingapi:pingapi

VOLUME /data/storage
EXPOSE 8080

# We override default config location search path,
# so that a custom file with defaults can be used
# Normally would use environment variables,
# but they take precedence over config file
# https://docs.spring.io/spring-boot/docs/1.5.6.RELEASE/reference/html/boot-features-external-config.html
ENV SPRING_CONFIG_LOCATION="optional:classpath:/,optional:classpath:/config/,file:./default.application.yaml,optional:file:./,optional:file:./config/"
COPY ./docker/default.application.yaml ./default.application.yaml

COPY --from=builder /build/build/libs/docker/serverpingapi.jar ./
CMD ["java", "-jar", "/app/serverpingapi.jar"]