package br.com.jtech.tasklist.config.infra.utils;

import java.util.UUID;

/**
 * Utility methods for generating UUID-based identifiers.
 */
public final class GenId {

    private GenId() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String newId() {
        return UUID.randomUUID().toString();
    }

    public static String newId(String id) {
        return (id != null && !id.isEmpty()) ? id : UUID.randomUUID().toString();
    }
}
