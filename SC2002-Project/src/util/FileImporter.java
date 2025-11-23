package util;

import java.io.*;
import java.util.*;

import entity.domain.Student;
import entity.domain.CareerCenterStaff;
import entity.domain.CompanyRepresentative;
import entity.domain.RegistrationRequest;
import entity.domain.enums.RequestStatus;
import repositories.UserRepository;
import repositories.RequestRepository;

/**
 * Utility FileImporter
 * Reads CSV/Excel-like data files and converts them into model objects.
 */
public class FileImporter {

    private final UserRepository userRepository;

    /**
     * Creates a new importer that auto-saves imported entries to the repository.
     *
     * @param userRepository repository to persist imported users into
     */
    public FileImporter(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository required");
    }

    /**
     * Imports student records from a CSV/Excel file.
     * Expected format: userId,name,password,year,major
     *
     * @param file CSV source
     * @return list of imported students
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
     *
     * @param file CSV source
     * @return list of imported career center staff
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
     *
     * @param file CSV source
     * @return raw tokenized records
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

    /**
     * Imports company representatives from CSV and seeds registration requests/status.
     * Supports both 7-col and 8-col (with password hash) formats.
     */
    public int importCompanyReps(File file, RequestRepository reqRepo) {
        if (file == null || !file.exists()) {
            System.out.println("File not found: " + file);
            return 0;
        }
        if (reqRepo != null) {
            reqRepo.clearRegistrations();
        }
        int imported = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                if (line.isBlank()) continue;
                String[] t = line.split(",", -1);
                if (t.length < 7) continue;
                String name = safeToken(t,1);
                String company = safeToken(t,2);
                String dept = safeToken(t,3);
                String pos = safeToken(t,4);
                String email = safeToken(t,5);
                String statusText = safeToken(t,6);
                String passwordHash = t.length >=8 ? safeToken(t,7) : "";
                CompanyRepresentative rep = new CompanyRepresentative(email, name, "password", company, dept, pos);
                if (!passwordHash.isBlank()) {
                    rep.setPasswordHashed(passwordHash);
                } else {
                    rep.setPassword("password");
                }
                try {
                    RequestStatus status = RequestStatus.valueOf(statusText);
                    rep.setApproved(status);
                } catch (Exception e) {
                    rep.setApproved(RequestStatus.PENDING);
                }
                userRepository.save(rep);
                if (reqRepo != null && rep.isApproved() == RequestStatus.PENDING) {
                    reqRepo.save(new RegistrationRequest(rep));
                }
                imported++;
            }
        } catch (IOException e) {
            System.err.println("Error reading company reps file: " + e.getMessage());
        }
        return imported;
    }

    /**
     * Safely retrieves a token from the array, trimming whitespace and handling bounds.
     *
     * @param tokens token array
     * @param index desired index
     * @return trimmed token or empty string if missing
     */
    private String safeToken(String[] tokens, int index) {
        if (index < 0 || index >= tokens.length) {
            return "";
        }
        String value = tokens[index];
        return value == null ? "" : value.trim();
    }
}
