# Based on https://github.com/PaperMC/bibliothek/blob/063cc014193b6ea160b3cabc7b93f70009e1fb2a/Dockerfile
# Build
FROM openjdk:17-jdk-slim AS builder

WORKDIR /build

COPY . .

RUN ./gradlew clean build --no-daemon

# Runtime
FROM openjdk:17-slim

WORKDIR /app

RUN groupadd --system pingapi \
    && useradd --system pingapi --gid pingapi \
    && chown -R pingapi:pingapi /app

USER pingapi:pingapi

EXPOSE 8080

COPY --from=builder /build/build/libs/ServerPingAPI.jar .

CMD ["java", "-jar", "/app/ServerPingAPI.jar"]