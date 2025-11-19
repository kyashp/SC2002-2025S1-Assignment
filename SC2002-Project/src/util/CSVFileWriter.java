package util;


import entity.domain.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.io.FileWriter;

public class CSVFileWriter {
    private static int count = 0;

    public static String repToWriteString(CompanyRepresentative r){
        count += 1;
        String result = String.format("%d,%s,%s,%s,%s,%s,%s",count,r.getUserName(),r.getCompanyName(),r.getDepartment(),r.getPosition(),r.getUserId(),r.isApproved().toString());
        return result;
    }
    public static void writeToFile(String filePath, String message){
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
    public static void writeRepStatus(String filePath, String userId, String status){
        File file = new File(filePath);

        List<String[]> reps = FileImporter.importCompanyReps(file);
        String header = "CompanyRepID,Name,CompanyName,Department,Position,Email,Status";

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
