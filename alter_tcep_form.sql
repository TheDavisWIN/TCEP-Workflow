-- SQL Script to alter TCEP_Form table for create/edit functionality
-- This allows storing form data directly in tcep_form without requiring all foreign key references
-- Written by GitHub Copilot
-- Updated to account for StartAdvisorID and CurrentAdvisorID columns added by advisortracking.sql

USE tcep;

-- Make foreign key columns nullable so forms can be saved as drafts
ALTER TABLE tcep_form 
    MODIFY COLUMN Equivalent_CourseID INT NULL,
    MODIFY COLUMN StatusID INT NULL;

-- Add columns to store basic form data directly (denormalized for easier form management)
-- Note: These are inserted AFTER CurrentAdvisorID which was added by advisortracking.sql
ALTER TABLE tcep_form
    ADD COLUMN Student_Name VARCHAR(150) NULL AFTER StudentID,
    ADD COLUMN Student_UtdID VARCHAR(20) NULL AFTER Student_Name,
    ADD COLUMN Student_NetID VARCHAR(50) NULL AFTER Student_UtdID,
    ADD COLUMN Incoming_CourseName VARCHAR(100) NULL AFTER Incoming_CourseID,
    ADD COLUMN Incoming_CourseNumber VARCHAR(20) NULL AFTER Incoming_CourseName,
    ADD COLUMN Incoming_CreditHours DECIMAL(4,2) NULL AFTER Incoming_CourseNumber,
    ADD COLUMN Institution_Name VARCHAR(200) NULL AFTER InstitutionID,
    ADD COLUMN Institution_Location VARCHAR(200) NULL AFTER Institution_Name,
    ADD COLUMN Equivalent_CourseName VARCHAR(100) NULL AFTER Equivalent_CourseID,
    ADD COLUMN Equivalent_CourseNumber VARCHAR(20) NULL AFTER Equivalent_CourseName;

-- Add a column to track if this is a draft or completed form
-- Insert AFTER CurrentAdvisorID instead of StatusID
ALTER TABLE tcep_form
    ADD COLUMN FormStatus ENUM('Draft', 'Submitted', 'UnderReview', 'Completed') DEFAULT 'Draft' AFTER CurrentAdvisorID;

-- Add columns for additional metadata
ALTER TABLE tcep_form
    ADD COLUMN CreatedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER FormStatus,
    ADD COLUMN LastModifiedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER CreatedDate,
    ADD COLUMN CreatedBy VARCHAR(50) NULL AFTER LastModifiedDate,
    ADD COLUMN LastModifiedBy VARCHAR(50) NULL AFTER CreatedBy;

-- Add index on frequently queried columns for performance
CREATE INDEX idx_student_netid ON tcep_form(Student_NetID);
CREATE INDEX idx_student_utdid ON tcep_form(Student_UtdID);
CREATE INDEX idx_form_status ON tcep_form(FormStatus);
CREATE INDEX idx_created_date ON tcep_form(CreatedDate);
CREATE INDEX idx_start_advisor ON tcep_form(StartAdvisorID);
CREATE INDEX idx_current_advisor ON tcep_form(CurrentAdvisorID);

-- Optional: Set default StatusID for new forms (assuming 1 = Pending)
UPDATE tcep_form SET StatusID = 1 WHERE StatusID IS NULL;
