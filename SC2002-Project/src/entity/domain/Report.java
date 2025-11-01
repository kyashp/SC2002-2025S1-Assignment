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

    public Report(List<ReportRow> rows) {
        this.generatedAt = LocalDateTime.now();
        this.rows = new ArrayList<>(rows);
    }

    // ===== Getters & Setters =====
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<ReportRow> getRows() {
        return new ArrayList<>(rows);
    }

    public void setRows(List<ReportRow> rows) {
        this.rows = new ArrayList<>(rows);
    }

    // ===== Utility Methods =====
    public void addRow(ReportRow row) {
        if (rows == null) rows = new ArrayList<>();
        rows.add(row);
    }

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
