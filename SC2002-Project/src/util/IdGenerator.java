package util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * <<Utility>> IdGenerator
 * Generates unique incremental IDs with a given prefix.
 * Thread-safe for use across different services.
 *
 * Example:
 *   newId("U") -> U001
 *   newId("APP") -> APP001
 */

public class IdGenerator {
	 // ===== Attributes =====
    // Stores a counter for each prefix used (e.g., U, APP, OPP)
    private static final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    // ===== Methods =====

    /**
     * Generates a new unique ID string with the given prefix.
     * Each prefix has its own independent counter.
     *
     * @param prefix the prefix for the ID (e.g., "S", "O", "A")
     * @return a new unique ID such as "S001" or "A023"
     */
    public String newId(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("Prefix cannot be null or blank.");
        }

        // Get or create the counter for this prefix
        AtomicInteger counter = counters.computeIfAbsent(prefix, p -> new AtomicInteger(0));

        // Increment and build ID with leading zeros
        int next = counter.incrementAndGet();
        return String.format("%s%03d", prefix.toUpperCase(), next);
    }

    /** Seed the counter to continue after existing max (used when importing). */
    public void seedPrefix(String prefix, int currentMax) {
        if (prefix == null || prefix.isBlank()) {
            return;
        }
        AtomicInteger counter = counters.computeIfAbsent(prefix, p -> new AtomicInteger(0));
        counter.updateAndGet(existing -> Math.max(existing, currentMax));
    }

    /**
     * Resets all ID counters (useful for testing or re-initializing the system).
     */
    public void resetAll() {
        counters.clear();
    }
}
