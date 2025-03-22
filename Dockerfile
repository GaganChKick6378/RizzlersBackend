FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download Maven dependencies (this layer will be cached if dependencies don't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw package -DskipTests

# Create a smaller runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Set environment variables
ARG DB_URL
ARG DB_USERNAME
ARG DB_PASSWORD
ARG ENVIRONMENT
ARG GRAPHQL_ENDPOINT
ARG GRAPHQL_API_KEY

ENV SPRING_DATASOURCE_URL=$DB_URL
ENV SPRING_DATASOURCE_USERNAME=$DB_USERNAME
ENV SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD
ENV SPRING_PROFILES_ACTIVE=$ENVIRONMENT
ENV APPLICATION_ENVIRONMENT=$ENVIRONMENT
ENV GRAPHQL_ENDPOINT=$GRAPHQL_ENDPOINT
ENV GRAPHQL_API_KEY=$GRAPHQL_API_KEY

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8080/api/health || exit 1 