# Test Suite Documentation

## Overview
This directory contains the automated test suite for the Rizzlers Backend application. The tests are designed to verify the functionality and reliability of the application's RDS (relational database) operations with a goal of achieving at least 30% code coverage.

## Test Strategy
The tests focus specifically on database operations and REST endpoints, avoiding any GraphQL-related functionality. This approach ensures coverage of:
- Database repository operations
- Service layer logic for database interactions
- REST controller endpoints

## Test Structure
- **Unit Tests**: These test individual components in isolation, mocking dependencies when necessary.
- **Integration Tests**: These test the interaction between components, such as controllers and their underlying services.
- **Repository Tests**: These test the JPA repository interfaces against an in-memory H2 database.

## Running Tests
You can run the tests using Maven:

```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn clean test jacoco:report
```

## Viewing Code Coverage
After running the tests with JaCoCo, a coverage report is generated in:
- `backend/target/site/jacoco/index.html`

Open this file in a web browser to view the coverage report. The report shows:
- Overall project coverage
- Coverage by package
- Coverage by class
- Detailed coverage of methods and lines

## Test Types Implemented

### Repository Tests
- `PropertyPromotionScheduleRepositoryTest`: Tests the repository methods directly against an H2 database:
  - `findAllByPropertyId`
  - `findActivePromotionsForPropertyInPeriod`

### Service Tests
- `RoomRateServiceTest`: Tests the `RoomRateService` implementation, focusing on methods that use RDS:
  - `getAllPromotions`
  - `getActivePromotions`

### Controller Tests
- `RoomRateControllerTest`: Tests the REST endpoints in `RoomRateController`, verifying:
  - Request/response structure
  - Proper integration with service layer
  - Correct handling of parameters

## Coverage Goal
The current tests aim to achieve at least 30% code coverage. The JaCoCo Maven plugin is configured to check for this minimum threshold and will fail the build if coverage drops below 30%.

## Test Configuration
- Tests use an in-memory H2 database instead of the production PostgreSQL database
- GraphQL functionality is disabled in test mode
- MockMvc is used for controller tests
- Repository tests use Spring's `@DataJpaTest` for database transactions

## Running the Tests
To run the tests and generate a coverage report:

```bash
cd backend
./run-tests.sh
```

## Troubleshooting
If you encounter issues with the Java version compatibility, check that:
1. You are using Java 17 or compatible version for running tests
2. The JaCoCo plugin is properly configured with exclusions for JDK classes

## Known Issues and Workarounds
- The project uses Spring Boot 3.4.x, which has deprecated the `@MockBean` annotation. Instead, we're using `@Import` with a `TestConfiguration` class to provide mock beans in controller tests.
- The `PropertyPromotionSchedule` entity does not have a `description` field/setter, so we've removed those assertions from our tests.

## Expanding Test Coverage
To increase test coverage:
1. Add tests for additional service implementations
2. Add tests for other controllers
3. Test error/exception paths in addition to happy paths
4. Add tests for utility classes and helpers

## Running the Tests
To run the tests and generate a coverage report:

```bash
cd backend
mvn clean test jacoco:report
```

After running this command, you can view the coverage report by opening `backend/target/site/jacoco/index.html` in your web browser. 