FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Install Maven
RUN apk add --no-cache maven

# Copy pom.xml first for better caching
COPY pom.xml .

# Copy source
COPY src src

# Define build arguments
ARG database_url
ARG database_username
ARG database_password
ARG graphql_endpoint
ARG graphql_api_key
ARG container_port
ARG allowed_origins
ARG ENV
ARG sql_init_mode=never

# Set environment variables for Maven build
ENV DATABASE_URL=$database_url \
    DATABASE_USERNAME=$database_username \
    DATABASE_PASSWORD=$database_password \
    GRAPHQL_ENDPOINT=$graphql_endpoint \
    GRAPHQL_API_KEY=$graphql_api_key \
    CONTAINER_PORT=$container_port \
    ALLOWED_ORIGINS=$allowed_origins \
    ENV=$ENV \
    SQL_INIT_MODE=$sql_init_mode

# Build application
RUN mvn install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Set environment variables for runtime
ENV DATABASE_URL=$database_url \
    DATABASE_USERNAME=$database_username \
    DATABASE_PASSWORD=$database_password \
    GRAPHQL_ENDPOINT=$graphql_endpoint \
    GRAPHQL_API_KEY=$graphql_api_key \
    CONTAINER_PORT=$container_port \
    ALLOWED_ORIGINS=$allowed_origins \
    ENV=$ENV \
    SQL_INIT_MODE=$sql_init_mode \
    CONTEXT_PATH=/api

ENTRYPOINT ["java","-cp","app:app/lib/*","com.kdu.rizzlers.RizzlersApplication"]

# Health check testing multiple paths to ensure at least one responds
HEALTHCHECK --interval=30s --timeout=10s --start-period=180s --retries=5 \
  CMD wget -q --spider http://localhost:${CONTAINER_PORT}/ || wget -q --spider http://localhost:${CONTAINER_PORT}/ping || wget -q --spider http://localhost:${CONTAINER_PORT}/health || wget -q --spider http://localhost:${CONTAINER_PORT}/api/health || exit 1

# Expose the container port
EXPOSE ${CONTAINER_PORT}