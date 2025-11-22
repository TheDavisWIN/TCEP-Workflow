INSERT INTO `department` (`DepartmentID`, `Department_Name`) VALUES ('1', 'Computer Science');
INSERT INTO `advisor` (`AdvisorID`, `Advisor_Name`, `Advisor_Email`, `DepartmentID`) VALUES ('1', 'Jane Doe', 'jxd654321', '1');
INSERT INTO `student` (`StudentID`, `Student_Name`, `Student_Email`, `DepartmentID`, `AdvisorID`) VALUES ('1', 'John Smith', 'jxs123456', '1', '1');
INSERT INTO `status_category` (`CategoryID`, `CategoryName`, `Description`) VALUES ('1', 'Pending', NULL);
INSERT INTO `transfer_status` (`StatusID`, `StatusName`, `CategoryID`) VALUES ('1', 'Pending', '1');
INSERT INTO `tcep_form` (
    `FormID`, `RequestDate`, `Term`, `Year`, `Degree_Requirement`, `Core_Designation`, `Supporting_Materials`,
    `DecisionDate`, `NotifiedDate`, `NotifiedMethod`, `StudentID`, `Incoming_CourseID`, `Equivalent_CourseID`,
    `InstitutionID`, `StatusID`) 
VALUES (
    '1', '2025-11-03', 'Spring', '2025', NULL, NULL, NULL, NULL, NULL, NULL, '1', '1515', '1', '2023', '1'
);