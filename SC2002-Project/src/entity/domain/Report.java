package entity.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete generated report containing multiple rows.
 */
public class Report {

    private LocalDateTime generatedAt;
    private List<ReportRow> rows;

    // ===== Constructors =====
    public Report() {
        this.generatedAt = LocalDateTime.now();
        this.rows = new ArrayList<>();
    }

    /**
     * Creates a report pre-populated with the provided rows.
     *
     * @param rows rows to include
     */
    public Report(List<ReportRow> rows) {
        this.generatedAt = LocalDateTime.now();
        this.rows = new ArrayList<>(rows);
    }

    // ===== Getters & Setters =====
    /** @return timestamp when the report was generated. */
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    /** @param generatedAt timestamp to assign to the report. */
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    /** @return defensive copy of the report rows. */
    public List<ReportRow> getRows() {
        return new ArrayList<>(rows);
    }

    /** @param rows replacement list of rows (copied internally). */
    public void setRows(List<ReportRow> rows) {
        this.rows = new ArrayList<>(rows);
    }

    // ===== Utility Methods =====
    /**
     * Adds a row to the report.
     *
     * @param row row to append
     */
    public void addRow(ReportRow row) {
        if (rows == null) rows = new ArrayList<>();
        rows.add(row);
    }

    /**
     * @return number of opportunities captured in the report
     */
    public int getTotalOpportunities() {
        return rows != null ? rows.size() : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Report generated at: ").append(generatedAt).append("\n");
        for (ReportRow row : rows) {
            sb.append(" - ").append(row).append("\n");
        }
        return sb.toString();
    }
}
