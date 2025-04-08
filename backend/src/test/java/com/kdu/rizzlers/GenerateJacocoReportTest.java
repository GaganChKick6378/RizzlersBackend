package com.kdu.rizzlers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * This class doesn't contain actual tests but is used as an entry point
 * to trigger JaCoCo report generation. The actual report generation is
 * configured in the pom.xml and will run automatically when mvn test is executed.
 */
@SpringBootTest
@ActiveProfiles("test")
public class GenerateJacocoReportTest {

    /**
     * This is a dummy test to ensure the application context loads properly.
     * The actual coverage is measured across all tests in the project.
     */
    @Test
    public void contextLoads() {
        // This test intentionally left empty
        // It's just to trigger test suite execution and JaCoCo report generation
    }
} 