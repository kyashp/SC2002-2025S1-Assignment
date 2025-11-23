# Internship Placement Management System (IPMS)

## Overview

The **Internship Placement Management System (IPMS)** is a command line interface Java-based application designed to facilitate the recruitment process between **Students**, **Company Representatives**, and **Career Center Staff**. 

The system allows companies to post internship opportunities, students to apply for them, and staff to moderate the process. It utilizes a **Boundary-Control-Entity (BCE)** architecture pattern to ensure separation of concerns and adheres to **SOLID** design principles. 

**Key Technical Highlights:**
*   **Architecture:** Boundary-Control-Entity (BCE)
*   **Persistence:** Local data storage using CSV files (automatic reload on major actions).
*   **Security:** Password hashing via `jBCrypt`.
*   **Design:** Modular package structure separating UI, Logic, and Data.

## Project Structure

The project is organized into specific packages separating the UI, business logic, data models, and persistence layers.
```
SC2002-Project/
├── data/ # CSV files used for data persistence
│ ├── applications.csv # Records of student applications
│ ├── opportunities.csv # Details of internship roles
│ ├── sample_staff_list.csv # Initial staff credentials
│ ├── sample_student_list.csv # Initial student credentials
│ ├── withdrawals.csv # Log of withdrawal requests
│ └── ...
│
├── lib/ # External dependencies
│ └── jbcrypt-0.4.jar # Library for secure password hashing
│
└── src/ # Source Code
├── boundary/ # View Layer (User Interface)
│ ├── ConsoleUI.java # Main menu router
│ ├── UIFactory.java # Factory to spawn role-specific UIs
│ └── [Role]UI.java # Specific screens for Student, Staff, etc.
│
├── control/ # Controller Layer (Business Logic)
│ ├── AuthService.java # Login, logout, and password management
│ ├── NotificationService.java # Generates alerts for users
│ ├── ReportService.java # Logic for generating statistical reports
│ └── [Entity]Service.java # Logic for Applications, Opportunities, etc.
│
├── entity/ # Model Layer
│ ├── domain/ # Core objects (User, Student, InternshipOpportunity)
│ └── domain/enums/ # Enumerations (Status, InternshipLevel, etc.)
│
├── main/ # Entry Point
│ └── ConsoleApp.java # Bootstraps the application and repositories
│
├── repositories/ # Data Access Layer
│ └── [Entity]Repository.java # Handles CRUD operations and CSV I/O
│
└── util/ # Utilities and Helpers
├── CSVFileWriter.java # Writes data back to CSVs
├── FileImporter.java # Reads initial data from CSVs
├── PasswordHasher.java # BCrypt wrapper for security
├── IdGenerator.java # Generates unique IDs (e.g., U001, O002)
└── Validator.java # Validates IDs, emails, and inputs
```
## Key Features

### Student
*   **Browse Opportunities:** View internships filtered by major and visibility settings.
*   **Apply:** Submit applications (Limit: 3 concurrent pending applications).
*   **Status Tracking:** Real-time view of application status (Pending, Successful, Rejected).
*   **Accept/Withdraw:** Accept offers or request withdrawal from applications.

### Company Representative
*   **Post Internships:** Create draft opportunities (Basic, Intermediate, Advanced levels) (Limit: 5 Internships).
*   **Management:** Edit drafts, delete opportunities, and toggle visibility.
*   **Candidate Selection:** Review applicants and mark them as *Successful* or *Unsuccessful*.

### Career Center Staff (Admin)
*   **Moderation:** Approve or reject new Company Representative accounts.
*   **Vetting:** Approve or reject internship postings before they go live.
*   **Administrative:** Process student withdrawal requests.
*   **Reporting:** Generate filtered reports on opportunities and placement rates.

## Getting Started

### Prerequisites
*   **Java Development Kit (JDK):** Version 17 or higher (required for switch expressions).
*   **External Libraries:** `jbcrypt-0.4.jar` (Included in `lib/`).

### Installation & Run
1.  **Clone** the repository to your local machine.
2.  Open your terminal and navigate to the root directory `SC2002-Project`.
3.  Execute the following commands to compile and run:
`
javac -d bin -cp "lib/" $(find src -name ".java")
java -cp "bin;lib/*" main.ConsoleApp
`

> **Note:** If the `data/` folder is missing, the application will attempt to create necessary files or load from sample CSVs if provided in the root.

## Default Credentials
Upon first login, all users are prompted to change their password.

*   **Default Password:** `password`
*   **Student ID:** Format `U...` (e.g., `U2310001A`)
*   **Staff ID:** NTU email prefix (e.g., `sng001@ntu.edu.sg`)

## Dependencies & Design Notes

*   **jBCrypt:** Used for secure password hashing and verification.
*   **Persistence Strategy:** The system automatically reloads data from CSVs on every major action (login, menu refresh) to ensure data consistency.
*   **ID Generation:** Uses atomic counters to ensure unique IDs for new entities (e.g., `O001` for opportunities, `W005` for withdrawals).
