package entity.domain;

import java.time.LocalDate;
import java.util.Objects;


/**
 * Represents a date range with a start and end date.
 * Used by ReportFilter and other classes for filtering data by period.
 */
public class DateRange {

    // ===== Attributes =====
    private LocalDate start;
    private LocalDate end;

    // ===== Constructors =====
    public DateRange() {
        // default: empty range
    }

    /**
     * Constructs a range with the specified start/end dates.
     *
     * @param start inclusive start date (nullable)
     * @param end inclusive end date (nullable, must not precede start)
     */
    public DateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        this.start = start;
        this.end = end;
    }

    // ===== Getters & Setters =====
    /**
     * @return inclusive start date (may be null)
     */
    public LocalDate getStart() {
        return start;
    }

    /**
     * @param start inclusive start date to assign
     */
    public void setStart(LocalDate start) {
        this.start = start;
    }

    /**
     * @return inclusive end date (may be null)
     */
    public LocalDate getEnd() {
        return end;
    }

    /**
     * @param end inclusive end date (must not precede start)
     */
    public void setEnd(LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        this.end = end;
    }

    // ===== Utility Methods =====

    /**
     * Checks if a given date is within this date range (inclusive).
     *
     * @param date date to evaluate
     * @return {@code true} if date is within range
     */
    public boolean contains(LocalDate date) {
        if (date == null) return false;
        boolean afterStart = (start == null || !date.isBefore(start));
        boolean beforeEnd = (end == null || !date.isAfter(end));
        return afterStart && beforeEnd;
    }

    /**
     * Checks if this range overlaps with another date range.
     *
     * @param other another range
     * @return {@code true} if the date windows overlap
     */
    public boolean overlaps(DateRange other) {
        if (other == null) return false;
        if (this.start == null || this.end == null || other.start == null || other.end == null) {
            return false;
        }
        return !(this.end.isBefore(other.start) || other.end.isBefore(this.start));
    }

    @Override
    public String toString() {
        return String.format("[%s to %s]",
                start != null ? start : "N/A",
                end != null ? end : "N/A");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateRange)) return false;
        DateRange other = (DateRange) o;
        return Objects.equals(start, other.start) && Objects.equals(end, other.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
