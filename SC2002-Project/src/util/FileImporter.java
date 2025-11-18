package util;

import java.io.*;
import java.util.*;

import entity.domain.Student;
import entity.domain.CareerCenterStaff;
import repositories.UserRepository;

/**
 * <<Utility>> FileImporter
 * Reads CSV/Excel-like data files and converts them into model objects.
 */
public class FileImporter {

    private final UserRepository userRepository;

    public FileImporter(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository required");
    }

    /**
     * Imports student records from a CSV/Excel file.
     * Expected format: userId,name,password,year,major
     */
    public List<Student> importStudents(File file) {
        List<Student> students = new ArrayList<>();
        if (file == null || !file.exists()) {
            System.out.println("File not found: " + file);
            return students;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                String[] t = line.split("[,\\t]");
                if (t.length < 5) continue;
                String id = t[0].trim(), name = t[1].trim();
                int year = Integer.parseInt(t[3].trim());
                String major = t[4].trim();
                Student s = new Student(id, name, year, major);
                userRepository.save(s);
                students.add(s);
            }
            System.out.println("Imported " + students.size() + " students from " + file.getName());
        } catch (IOException e) {
            System.err.println("Error reading student file: " + e.getMessage());
        }
        return students;
    }

    /**
     * Imports staff records from a CSV/Excel file.
     * Expected format: userId,name,password,department
     */
    public List<CareerCenterStaff> importStaff(File file) {
        List<CareerCenterStaff> staffList = new ArrayList<>();
        if (file == null || !file.exists()) {
            System.out.println("File not found: " + file);
            return staffList;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                String[] t = line.split("[,\\t]");
                if (t.length < 4) continue;
                String id = t[4].trim(), name = t[1].trim(), dept = t[3].trim();
                CareerCenterStaff staff = new CareerCenterStaff(id, name, dept);
                userRepository.save(staff);
                staffList.add(staff);
            }
            System.out.println("Imported " + staffList.size() + " staff from " + file.getName());
        } catch (IOException e) {
            System.err.println("Error reading staff file: " + e.getMessage());
        }
        return staffList;
    }

    /**
     * Imports company representative records from a CSV/Excel file.
     * Supports both legacy (userId,name,password,company,dept,position) and new
     * (CompanyRepID,Name,CompanyName,Department,Position,Email,Status) formats.
     */
    public static List<String []> importCompanyReps(File file) {
        List<String []> reps = new ArrayList<>();
        if (file == null || !file.exists()) {
            System.out.println("File not found: " + file);
            return reps;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            boolean newFormat = false;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                String[] t = line.split("[,\\t]");
                reps.add(t);
            }
        } catch (IOException e) {
            System.err.println("Error reading company reps file: " + e.getMessage());
        }
        return reps;
    }

    private String safeToken(String[] tokens, int index) {
        if (index < 0 || index >= tokens.length) {
            return "";
        }
        String value = tokens[index];
        return value == null ? "" : value.trim();
    }
}
