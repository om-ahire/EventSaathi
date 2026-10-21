package com.eventsaathi.entity.enums;

/**
 * The three roles in EventSaathi. Stored as a VARCHAR in the DB via @Enumerated(STRING).
 * ROLE_ prefix is Spring Security convention – GrantedAuthority names must start with it
 * when using hasRole(), but you can use hasAuthority("ROLE_ADMIN") explicitly too.
 */
public enum RoleName {
    ROLE_CUSTOMER,
    ROLE_VENDOR,
    ROLE_ADMIN
}
