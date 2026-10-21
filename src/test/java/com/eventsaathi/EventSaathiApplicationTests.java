package com.eventsaathi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test – verifies the Spring application context loads without errors.
 *
 * WHY @SpringBootTest?
 *   It starts the FULL application context (web server, DB, security) so any
 *   misconfigured bean will fail here, not in production. This is the minimum
 *   test every Spring Boot project should have.
 *
 * The active "test" profile configures an in-memory H2 database, so this test
 * does not require a running MySQL instance.
 */
@SpringBootTest
@ActiveProfiles("test")
class EventSaathiApplicationTests {

    @Test
    void contextLoads() {
        // If this method runs without throwing, the context loaded successfully.
        // No assertions needed – a failed context startup will fail the test.
    }
}
