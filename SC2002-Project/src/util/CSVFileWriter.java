package util;


import entity.domain.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.io.FileWriter;
import java.nio.file.Files;

/**
 * Utility for writing company representative CSV files and entries.
 */
public class CSVFileWriter {
    private static final String REP_HEADER = "CompanyRepID,Name,CompanyName,Department,Position,Email,Status,PasswordHash";

    /**
     * Builds a CSV row representing the given representative.
     *
     * @param r representative to format
     * @return CSV string
     */
    public static String repToWriteString(CompanyRepresentative r){
        long lineCount = 1;
        File f = new File("data/sample_company_representative_list.csv");
        try { if (f.exists()) lineCount = Files.lines(f.toPath()).count(); } catch (Exception ignored) {}
        long idx = Math.max(1, lineCount); // header counts as 1, so first rep gets id 1
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                idx,
                r.getUserName(),
                r.getCompanyName(),
                r.getDepartment(),
                r.getPosition(),
                r.getUserId(),
                r.isApproved().toString(),
                r.getPassword()
        );
    }

    /** Ensures the rep CSV exists with header. */
    public static void ensureRepCsv(String filePath) {
        try {
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();
            if (!file.exists() || file.length() == 0) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
                    writer.write(REP_HEADER);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize representative CSV: " + e.getMessage());
        }
    }
    /**
     * Appends a message to the target CSV file, creating the file if needed.
     *
     * @param filePath path to CSV file
     * @param message line to append
     */
    public static void writeToFile(String filePath, String message){
        ensureRepCsv(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {

            // --- 1. Write the Data Records ---
            writer.write(message);
            writer.newLine();;

            // Flush the stream to ensure all buffered data is written to the file system
            writer.flush();

        } catch (IOException e) {
            System.err.println("An error occurred while writing the CSV file: " + e.getMessage());
        }
    }
    /**
     * Rewrites the representative CSV file after updating the status of a specific user.
     *
     * @param filePath CSV path
     * @param userId representative email/id
     * @param status status text to set
     */
    public static void writeRepStatus(String filePath, String userId, String status){
        File file = new File(filePath);
        ensureRepCsv(filePath);

        List<String[]> reps = FileImporter.importCompanyReps(file);
        String header = REP_HEADER;

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath,false))){

            writer.write(header);
            writer.newLine();;

            for(String [] row: reps){
                if(row[5].equals(userId)){
                    row[6] = status;
                }
                writer.write(String.join(",", row));
                writer.newLine();
            }
            writer.flush();
        } catch(IOException e) {
            System.err.println("An error occurred while writing the CSV file: " + e.getMessage());
        }
    }
}
