-- Migration: create status/history table for form actions
CREATE TABLE IF NOT EXISTS `tcep_form_status_history` (
  `HistoryID` INT AUTO_INCREMENT PRIMARY KEY,
  `FormID` INT NOT NULL,
  `ActionType` VARCHAR(50) NOT NULL,
  `Comments` TEXT,
  `AssignedAdvisorID` INT NULL,
  `DepartmentName` VARCHAR(200) NULL,
  `ActionDate` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Optional: index on FormID for faster lookups
CREATE INDEX IF NOT EXISTS idx_tcep_form_status_history_formid ON tcep_form_status_history(FormID);
