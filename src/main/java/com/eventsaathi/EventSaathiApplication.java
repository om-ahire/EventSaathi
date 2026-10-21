package com.eventsaathi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EventSaathi – Multi-vendor event planning platform.
 *
 * @SpringBootApplication is a convenience annotation that combines:
 *   - @Configuration   → marks this class as a source of bean definitions
 *   - @EnableAutoConfiguration → tells Spring Boot to start adding beans based
 *     on classpath settings, other beans, and various property settings
 *   - @ComponentScan   → tells Spring to look for components in this package
 *     and its sub-packages (com.eventsaathi.*)
 */
@SpringBootApplication
public class EventSaathiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventSaathiApplication.class, args);
    }
}
