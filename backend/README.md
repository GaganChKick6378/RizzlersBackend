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