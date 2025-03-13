# Rizzlers Backend - Student Management System

This is a Spring Boot application for managing college student records.

## Prerequisites

- Java 17 or higher
- Maven 3.6.3 or higher
- PostgreSQL 12 or higher

## Database Setup

1. Install PostgreSQL and pgAdmin if you haven't already.
2. Create two databases in pgAdmin:
   - `rizzlers_dev` - For development environment
   - `rizzlers_qa` - For QA environment
3. The application will automatically create tables and populate sample data when it starts.

## Configuration

The application uses different configurations for different environments:

- **Development**: `application-dev.properties`
- **QA**: `application-qa.properties`
- **Default**: `application.properties` (points to dev by default)

### Database Configuration

Update the database connection settings in the appropriate properties file:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/rizzlers_dev
spring.datasource.username=postgres
spring.datasource.password=postgres
```

## Running the Application

### Development Environment

```bash
# Using Maven
mvn spring-boot:run -Dspring.profiles.active=dev

# Using Java
java -jar target/IBE-0.0.1-SNAPSHOT.jar -Dspring.profiles.active=dev
```

### QA Environment

```bash
# Using Maven
mvn spring-boot:run -Dspring.profiles.active=qa

# Using Java
java -jar target/IBE-0.0.1-SNAPSHOT.jar -Dspring.profiles.active=qa
```

## API Endpoints

### Health Check
- `GET /api/health` - Check application health

### Student Management
- `GET /api/students` - Get all students
- `GET /api/students/{id}` - Get student by ID
- `GET /api/students/enrollment/{enrollmentNumber}` - Get student by enrollment number
- `GET /api/students/department/{department}` - Get students by department
- `GET /api/students/year/{yearOfStudy}` - Get students by year of study
- `GET /api/students/cgpa/{cgpa}` - Get students with CGPA greater than or equal to value
- `POST /api/students` - Create a new student
- `PUT /api/students/{id}` - Update an existing student
- `DELETE /api/students/{id}` - Delete a student (soft delete)

## Sample Data

The application is pre-loaded with sample student data across various departments:
- Computer Science
- Electrical Engineering
- Mechanical Engineering
- Business Administration

This data will be automatically loaded when the application starts.

## Security

The application uses basic authentication:
- Username: admin
- Password: admin

## Additional Information

- The application automatically creates tables and indexes if they don't exist.
- Logging levels can be adjusted in the respective properties files.
- CORS is configured to allow requests from the frontend application.

# Rizzlers Backend

This directory contains the SpringBoot application for the Rizzlers backend.

## Docker Image

The `Dockerfile` in this directory is used to build the container image that will be deployed to Amazon ECS. This happens automatically through the GitHub Actions CI/CD pipeline, but you can also build it locally for testing.

### Building Locally

To build the Docker image locally:

```bash
docker build -t rizzlers-backend:local .
```

To run the container locally:

```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/your-db-name \
  -e SPRING_DATASOURCE_USERNAME=your-username \
  -e SPRING_DATASOURCE_PASSWORD=your-password \
  rizzlers-backend:local
```

### CI/CD Pipeline

When you push to the `dev` branch, the GitHub Actions workflow:

1. Builds the application with Maven
2. Builds the Docker image
3. Pushes the image to Amazon ECR
4. Deploys the new image to ECS

## Environment Variables

The application expects the following environment variables:

- `SPRING_DATASOURCE_URL`: JDBC URL for the PostgreSQL database
- `SPRING_DATASOURCE_USERNAME`: Database username
- `SPRING_DATASOURCE_PASSWORD`: Database password
- `APPLICATION_ENVIRONMENT`: Either "Development" or "Testing"

These are set in the ECS task definition. 