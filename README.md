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
├── data/
│ ├── applications.csv
│ ├── opportunities.csv
│ ├── sample_staff_list.csv
│ ├── sample_student_list.csv
│ ├── withdrawals.csv
│ └── ...
│
├── doc/ (JavaDocs)
├── lib/ # External dependencies
│ └── jbcrypt-0.4.jar
│
└── src/
├── boundary/
│ ├── ConsoleUI.java
│ ├── UIFactory.java
│ └── [Role]UI.java
│
├── control/
│ ├── AuthService.java
│ ├── NotificationService.java
│ ├── ReportService.java
│ └── [Entity]Service.java
│
├── entity/
│ ├── domain/
│ └── domain/enums/
│
├── main/
│ └── ConsoleApp.java
│
├── repositories/
│ └── [Entity]Repository.java
│
└── util/
├── CSVFileWriter.java
├── FileImporter.java
├── PasswordHasher.java
├── IdGenerator.java
└── Validator.java
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
