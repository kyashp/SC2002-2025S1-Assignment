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

    public DateRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        this.start = start;
        this.end = end;
    }

    // ===== Getters & Setters =====
    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }
        this.end = end;
    }

    // ===== Utility Methods =====

    /**
     * Checks if a given date is within this date range (inclusive).
     */
    public boolean contains(LocalDate date) {
        if (date == null) return false;
        boolean afterStart = (start == null || !date.isBefore(start));
        boolean beforeEnd = (end == null || !date.isAfter(end));
        return afterStart && beforeEnd;
    }

    /**
     * Checks if this range overlaps with another date range.
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
