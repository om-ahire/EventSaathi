package com.eventsaathi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DatabaseConnectivityCheck runs once immediately after the application context
 * is fully loaded (but before it starts serving requests).
 *
 * WHY ApplicationRunner and not CommandLineRunner?
 *   ApplicationRunner receives parsed ApplicationArguments; CommandLineRunner
 *   gets raw String[]. Both run at the same lifecycle point. We use
 *   ApplicationRunner here as a habit – it's more testable.
 *
 * INTERVIEW TALKING POINT:
 *   This is the "fail-fast" principle – if the database is unreachable, we want
 *   to know immediately on startup rather than at the first incoming request.
 *   HikariCP already validates the pool, but this gives us a human-readable log
 *   with the server version, which is handy during CI/CD pipeline debugging.
 */
@Component
public class DatabaseConnectivityCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectivityCheck.class);

    // Constructor injection – no @Autowired field injection (matches architecture rules)
    private final DataSource dataSource;

    public DatabaseConnectivityCheck(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("═══════════════════════════════════════════════");
        log.info("  EventSaathi – Database Connectivity Check");
        log.info("═══════════════════════════════════════════════");

        try (Connection conn = dataSource.getConnection()) {
        String productName = conn.getMetaData().getDatabaseProductName();
        String productVersion = conn.getMetaData().getDatabaseProductVersion();

        log.info("Database connected successfully: {} {}", productName, productVersion);
        log.info("Schema: {}", conn.getCatalog());

        } catch (SQLException e) {
            throw new IllegalStateException("Database connectivity check failed", e);
        }

        log.info("═══════════════════════════════════════════════");
    }
}
