-- Complete setup for TCEP form transfer system
-- Run this in phpMyAdmin after selecting the tcep database

-- 1. Create the status history table (required for form transfer)
CREATE TABLE IF NOT EXISTS `tcep_form_status_history` (
  `HistoryID` INT AUTO_INCREMENT PRIMARY KEY,
  `FormID` INT NOT NULL,
  `ActionType` VARCHAR(50) NOT NULL,
  `Comments` TEXT,
  `AssignedAdvisorID` INT NULL,
  `DepartmentName` VARCHAR(200) NULL,
  `ActionDate` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX `idx_form_id` (`FormID`),
  INDEX `idx_assigned_advisor` (`AssignedAdvisorID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 2. Clear existing test data
DELETE FROM `tcep_form` WHERE `FormID` IN (1, 2, 3, 4, 5, 6);
DELETE FROM `student` WHERE `StudentID` IN (1, 2, 3, 4, 5, 6);
DELETE FROM `advisor` WHERE `AdvisorID` IN (1, 2);
DELETE FROM `department` WHERE `DepartmentID` = 1;
DELETE FROM `transfer_status` WHERE `StatusID` IN (1, 2, 3);
DELETE FROM `status_category` WHERE `CategoryID` IN (1, 2, 3);

-- 3. Insert test data
INSERT INTO `department` (`DepartmentID`, `Department_Name`) VALUES ('1', 'Computer Science');

INSERT INTO `advisor` (`AdvisorID`, `Advisor_Name`, `Advisor_Email`, `DepartmentID`) VALUES 
('1', 'Jane Doe', 'jxd654321', '1'),
('2', 'John Doe', 'jxd123456', '1');

-- Students assigned to Jane Doe (AdvisorID = 1)
INSERT INTO `student` (`StudentID`, `Student_Name`, `Student_Email`, `DepartmentID`, `AdvisorID`) VALUES 
('1', 'Alice Johnson', 'axj123456', '1', '1'),
('2', 'Bob Williams', 'bxw123456', '1', '1'),
('3', 'Carol Davis', 'cxd123456', '1', '1');

-- Students assigned to John Doe (AdvisorID = 2)
INSERT INTO `student` (`StudentID`, `Student_Name`, `Student_Email`, `DepartmentID`, `AdvisorID`) VALUES 
('4', 'David Miller', 'dxm123456', '1', '2'),
('5', 'Emma Wilson', 'exw123456', '1', '2'),
('6', 'Frank Brown', 'fxb123456', '1', '2');

INSERT INTO `status_category` (`CategoryID`, `CategoryName`, `Description`) VALUES 
('1', 'Pending', NULL),
('2', 'Approved', NULL),
('3', 'Denied', NULL);

INSERT INTO `transfer_status` (`StatusID`, `StatusName`, `CategoryID`) VALUES 
('1', 'Pending', '1'),
('2', 'Approved', '2'),
('3', 'Denied', '3');

-- Forms for Jane Doe's students (using NetID - lowercase to match actual column)
INSERT INTO `tcep_form` (
    `FormID`, `RequestDate`, `Term`, `Year`, `Degree_Requirement`, `Core_Designation`, `Supporting_Materials`,
    `DecisionDate`, `NotifiedDate`, `NotifiedMethod`, `StudentID`, `Incoming_CourseID`, `Equivalent_CourseID`,
    `InstitutionID`, `StatusID`, `NetID`) 
VALUES 
('1', '2025-11-01', 'Spring', '2025', 'Core CS', 'CS', NULL, NULL, NULL, NULL, '1', '1515', '1', '2023', '1', 'axj123456'),
('2', '2025-11-05', 'Fall', '2025', 'Elective', NULL, NULL, NULL, NULL, NULL, '2', '1515', '1', '2023', '1', 'bxw123456'),
('3', '2025-11-10', 'Spring', '2025', 'Core CS', 'CS', NULL, NULL, NULL, NULL, '3', '1515', '1', '2023', '1', 'cxd123456');

-- Forms for John Doe's students
INSERT INTO `tcep_form` (
    `FormID`, `RequestDate`, `Term`, `Year`, `Degree_Requirement`, `Core_Designation`, `Supporting_Materials`,
    `DecisionDate`, `NotifiedDate`, `NotifiedMethod`, `StudentID`, `Incoming_CourseID`, `Equivalent_CourseID`,
    `InstitutionID`, `StatusID`, `NetID`) 
VALUES 
('4', '2025-11-02', 'Fall', '2025', 'Core CS', 'CS', NULL, NULL, NULL, NULL, '4', '1515', '1', '2023', '1', 'dxm123456'),
('5', '2025-11-07', 'Spring', '2025', 'Elective', NULL, NULL, NULL, NULL, NULL, '5', '1515', '1', '2023', '1', 'exw123456'),
('6', '2025-11-12', 'Fall', '2025', 'Core CS', 'CS', NULL, NULL, NULL, NULL, '6', '1515', '1', '2023', '1', 'fxb123456');

SELECT 'Setup complete! You should now have:' AS Status;
SELECT COUNT(*) AS 'Advisors' FROM advisor;
SELECT COUNT(*) AS 'Students' FROM student;
SELECT COUNT(*) AS 'Forms' FROM tcep_form;
SELECT 'Status history table created' AS 'tcep_form_status_history';
