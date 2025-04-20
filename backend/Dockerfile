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
ARG encryption_secret_key
ARG email_app_password
ARG frontend_review_url
# AWS Cognito Configuration
ARG aws_cognito_region
ARG aws_cognito_user_pool_id
ARG aws_cognito_app_client_id
ARG aws_cognito_jwk_url
# JWT Configuration
ARG jwt_secret
ARG jwt_expiration
ARG cache_redis_host
ARG cache_redis_port
ARG cache_redis_ssl

# Set environment variables for Maven build
ENV DATABASE_URL=$database_url \
    DATABASE_USERNAME=$database_username \
    DATABASE_PASSWORD=$database_password \
    GRAPHQL_ENDPOINT=$graphql_endpoint \
    GRAPHQL_API_KEY=$graphql_api_key \
    CONTAINER_PORT=$container_port \
    ALLOWED_ORIGINS=$allowed_origins \
    ENV=$ENV \
    SQL_INIT_MODE=$sql_init_mode \
    ENCRYPTION_SECRET_KEY=$encryption_secret_key \
    EMAIL_APP_PASSWORD=$email_app_password \
    FRONTEND_REVIEW_URL=$frontend_review_url \
    CACHE_REDIS_HOST=$cache_redis_host \
    CACHE_REDIS_PORT=$cache_redis_port \
    CACHE_REDIS_SSL=$cache_redis_ssl \
    # AWS Cognito Environment variables
    AWS_COGNITO_REGION=$aws_cognito_region \
    AWS_COGNITO_USER_POOL_ID=$aws_cognito_user_pool_id \
    AWS_COGNITO_APP_CLIENT_ID=$aws_cognito_app_client_id \
    AWS_COGNITO_JWK_URL=$aws_cognito_jwk_url \
    # JWT Environment variables
    JWT_SECRET=$jwt_secret \
    JWT_EXPIRATION=$jwt_expiration

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
    CONTEXT_PATH=/api \
    ENCRYPTION_SECRET_KEY=$encryption_secret_key \
    EMAIL_APP_PASSWORD=$email_app_password \
    FRONTEND_REVIEW_URL=$frontend_review_url \
    CACHE_REDIS_HOST=$cache_redis_host \
    CACHE_REDIS_PORT=$cache_redis_port \
    CACHE_REDIS_SSL=$cache_redis_ssl \
    # AWS Cognito Environment variables
    AWS_COGNITO_REGION=$aws_cognito_region \
    AWS_COGNITO_USER_POOL_ID=$aws_cognito_user_pool_id \
    AWS_COGNITO_APP_CLIENT_ID=$aws_cognito_app_client_id \
    AWS_COGNITO_JWK_URL=$aws_cognito_jwk_url \
    # JWT Environment variables
    JWT_SECRET=$jwt_secret \
    JWT_EXPIRATION=$jwt_expiration

# JVM optimization for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -Djava.security.egd=file:/dev/./urandom -Dspring.jmx.enabled=false -Dspring.config.location=classpath:/application.properties -Dspring.main.lazy-initialization=true"

ENTRYPOINT exec java $JAVA_OPTS -cp app:app/lib/* com.kdu.rizzlers.RizzlersApplication
# Expose the container port
EXPOSE ${CONTAINER_PORT}