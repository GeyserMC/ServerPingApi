FROM eclipse-temurin:17-alpine

# Install curl for the healthcheck
RUN apk update && apk add curl

RUN addgroup -S pingapi \
    && adduser -S pingapi -G pingapi \
    && chown -R pingapi:pingapi /opt/app
USER pingapi:pingapi

WORKDIR /opt/app

COPY build/libs/ServerPingApi.jar ServerPingApi.jar
COPY ./docker/default.application.yaml ./default.application.yaml

EXPOSE 8080

# We override default config location search path,
# so that a custom file with defaults can be used
# Normally would use environment variables,
# but they take precedence over config file
# https://docs.spring.io/spring-boot/docs/1.5.6.RELEASE/reference/html/boot-features-external-config.html
ENV SPRING_CONFIG_LOCATION="optional:classpath:/,optional:classpath:/config/,file:./default.application.yaml,optional:file:./,optional:file:./config/"

HEALTHCHECK --interval=30s --timeout=30s --start-period=5s \
    --retries=3 CMD [ "sh", "-c", "echo -n 'curl localhost:8080... '; \
    (\
        curl -sf localhost:8080/health > /dev/null\
    ) && echo OK || (\
        echo Fail && exit 2\
    )"]


CMD ["java", "-jar", "ServerPingApi.jar"]
