-- Completely reset the database
DROP DATABASE IF EXISTS tcep;
CREATE DATABASE tcep;
USE tcep;

-- 1. Base Reference Tables

-- Department Table
CREATE TABLE Department (
    DepartmentID INT AUTO_INCREMENT PRIMARY KEY,
    Department_Name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- Institution Table
CREATE TABLE Institution (
    InstitutionID INT AUTO_INCREMENT PRIMARY KEY,
    Institution_Name VARCHAR(150) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- Status Category Table
-- Differentiates between overall status groups like Approved, Denied, Sent Back,
CREATE TABLE Status_Category (
    CategoryID INT AUTO_INCREMENT PRIMARY KEY,
    CategoryName ENUM('Pending', 'Approved', 'Denied', 'Sent Back') NOT NULL UNIQUE,
    Description VARCHAR(255)
) ENGINE=InnoDB;

-- Transfer Status Table
-- Each status (e.g., "Awaiting Review", "Sent Back for Info") belongs to a Category
CREATE TABLE Transfer_Status (
    StatusID INT AUTO_INCREMENT PRIMARY KEY,
    StatusName VARCHAR(100) NOT NULL UNIQUE,
    CategoryID INT NOT NULL,
    FOREIGN KEY (CategoryID) REFERENCES Status_Category(CategoryID)
) ENGINE=InnoDB;

-- Grade Table
CREATE TABLE Grade (
    GradeID INT AUTO_INCREMENT PRIMARY KEY,
    GradeCode CHAR(2) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- 2. People and Department Relations

-- Advisor Table
CREATE TABLE Advisor (
    AdvisorID INT AUTO_INCREMENT PRIMARY KEY,
    Advisor_Name VARCHAR(100) NOT NULL,
    Advisor_Email VARCHAR(100) NOT NULL UNIQUE,
    DepartmentID INT NOT NULL,
    FOREIGN KEY (DepartmentID) REFERENCES Department(DepartmentID)
) ENGINE=InnoDB;

-- Student Table
CREATE TABLE Student (
    StudentID INT AUTO_INCREMENT PRIMARY KEY,
    Student_Name VARCHAR(100) NOT NULL,
    Student_Email VARCHAR(100) NOT NULL UNIQUE,
    DepartmentID INT NOT NULL,
    AdvisorID INT,
    FOREIGN KEY (DepartmentID) REFERENCES Department(DepartmentID),
    FOREIGN KEY (AdvisorID) REFERENCES Advisor(AdvisorID)
) ENGINE=InnoDB;

-- 3. Courses and Equivalencies

-- Incoming Course Table
CREATE TABLE Incoming_Course (
    Incoming_CourseID INT AUTO_INCREMENT PRIMARY KEY,
    CourseName VARCHAR(150) NOT NULL,
    CoursePrefix CHAR(4) NOT NULL,
    CourseNumber CHAR(4) NOT NULL,
    InstitutionID INT NOT NULL,
    DepartmentID INT NOT NULL,
    FOREIGN KEY (InstitutionID) REFERENCES Institution(InstitutionID),
    FOREIGN KEY (DepartmentID) REFERENCES Department(DepartmentID)
) ENGINE=InnoDB;

-- Equivalent Course Table
CREATE TABLE Equivalent_Course (
    Equivalent_CourseID INT AUTO_INCREMENT PRIMARY KEY,
    CourseName VARCHAR(50) NOT NULL,
    CoursePrefix CHAR(4) NOT NULL,
    CourseNumber CHAR(4) NOT NULL,
    Credit_Earned DECIMAL(4,2),
    DepartmentID INT NOT NULL,
    FOREIGN KEY (DepartmentID) REFERENCES Department(DepartmentID)
) ENGINE=InnoDB;

-- Course Equivalency Table (junction)
CREATE TABLE Course_Equivalency (
    Incoming_CourseID INT NOT NULL,
    Equivalent_CourseID INT NOT NULL,
    PRIMARY KEY (Incoming_CourseID, Equivalent_CourseID),
    FOREIGN KEY (Incoming_CourseID) REFERENCES Incoming_Course(Incoming_CourseID),
    FOREIGN KEY (Equivalent_CourseID) REFERENCES Equivalent_Course(Equivalent_CourseID)
) ENGINE=InnoDB;

-- 4. Forms, Transfers, and Teams

-- TCEP Form Table
CREATE TABLE TCEP_Form (
    FormID INT AUTO_INCREMENT PRIMARY KEY,
    RequestDate DATE NOT NULL,
    Term ENUM('Fall', 'Spring', 'Summer', 'Other') NOT NULL,
    Year YEAR NOT NULL,
    Degree_Requirement VARCHAR(150),
    Core_Designation VARCHAR(3),
    Supporting_Materials BLOB,
    DecisionDate DATE,
    NotifiedDate DATE,
    NotifiedMethod VARCHAR(50),
    StudentID INT NOT NULL,
    Incoming_CourseID INT NOT NULL,
    Equivalent_CourseID INT,
    InstitutionID INT NOT NULL,
    StatusID INT NOT NULL,
    FOREIGN KEY (StudentID) REFERENCES Student(StudentID),
    FOREIGN KEY (Incoming_CourseID) REFERENCES Incoming_Course(Incoming_CourseID),
    FOREIGN KEY (Equivalent_CourseID) REFERENCES Equivalent_Course(Equivalent_CourseID),
    FOREIGN KEY (InstitutionID) REFERENCES Institution(InstitutionID)
) ENGINE=InnoDB;

-- Transfers Table
CREATE TABLE Transfers (
    TransferID INT AUTO_INCREMENT PRIMARY KEY,
    Source_Institution VARCHAR(100),
    Credit_Earned DECIMAL(4,2),
    StudentID INT NOT NULL,
    DepartmentID INT NOT NULL,
    Incoming_CourseID INT NOT NULL,
    Equivalent_CourseID INT NOT NULL,
    GradeID INT NOT NULL,
    StatusID INT NOT NULL,
    FormID INT,
    FOREIGN KEY (StudentID) REFERENCES Student(StudentID),
    FOREIGN KEY (DepartmentID) REFERENCES Department(DepartmentID),
    FOREIGN KEY (Incoming_CourseID) REFERENCES Incoming_Course(Incoming_CourseID),
    FOREIGN KEY (Equivalent_CourseID) REFERENCES Equivalent_Course(Equivalent_CourseID),
    FOREIGN KEY (GradeID) REFERENCES Grade(GradeID),
    FOREIGN KEY (StatusID) REFERENCES Transfer_Status(StatusID),
    FOREIGN KEY (FormID) REFERENCES TCEP_Form(FormID)
) ENGINE=InnoDB;

-- Team Members Table
CREATE TABLE Team_Members (
    TeamMemberID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Role VARCHAR(50)
) ENGINE=InnoDB;

-- Transfers Team (junction)
CREATE TABLE Transfers_Team (
    TransferID INT NOT NULL,
    TeamMemberID INT NOT NULL,
    PRIMARY KEY (TransferID, TeamMemberID),
    FOREIGN KEY (TransferID) REFERENCES Transfers(TransferID),
    FOREIGN KEY (TeamMemberID) REFERENCES Team_Members(TeamMemberID)
) ENGINE=InnoDB;

-- 5. Status History

-- TCEP Status History Table
CREATE TABLE TCEP_Status_History (
    History_ID INT AUTO_INCREMENT PRIMARY KEY,
    Changed_On DATETIME NOT NULL,
    Comments TEXT,
    FormID INT NOT NULL,
    StatusID INT NOT NULL,
    AdvisorID INT,
    StudentID INT,
    FOREIGN KEY (FormID) REFERENCES TCEP_Form(FormID),
    FOREIGN KEY (StatusID) REFERENCES Transfer_Status(StatusID),
    FOREIGN KEY (AdvisorID) REFERENCES Advisor(AdvisorID),
    FOREIGN KEY (StudentID) REFERENCES Student(StudentID)
) ENGINE=InnoDB;
