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
 * <<utility>> FileImporter
 * Reads CSV/Excel-like data files and converts them into model objects.
 */
public class FileImporter {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    public FileImporter(UserRepository userRepository, RequestRepository requestRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository required");
        this.requestRepository = Objects.requireNonNull(requestRepository, "RequestRepository required");
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
     * Expected format: userId,name,password,companyName,department,position
     */
    public List<CompanyRepresentative> importCompanyReps(File file) {
        List<CompanyRepresentative> reps = new ArrayList<>();
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
                if (t.length < 6) continue;

                String id = t[0].trim(), name = t[1].trim(), password = t[2].trim();
                String company = t[3].trim(), dept = t[4].trim(), position = t[5].trim();

                CompanyRepresentative rep = new CompanyRepresentative(id, name, password, company, dept, position);
                rep.setApproved(false); // new reps are not approved initially
                userRepository.save(rep);
                reps.add(rep);

                // create corresponding registration request
                RegistrationRequest req = new RegistrationRequest();
                req.setRep(rep);
                req.setStatus(RequestStatus.PENDING);
                req.setRequestedAt(java.time.LocalDateTime.now());
                requestRepository.save(req);
            }

            System.out.println("Imported " + reps.size() + " company reps from " + file.getName());
        } catch (IOException e) {
            System.err.println("Error reading company reps file: " + e.getMessage());
        }
        return reps;
    }
}
