USE tcep;

-- 1. Add new advisor ID columns
ALTER TABLE tcep_form
    ADD COLUMN StartAdvisorID   INT NULL AFTER StatusID,
    ADD COLUMN CurrentAdvisorID INT NULL AFTER StartAdvisorID;

-- 2. Add foreign keys so these IDs must match real advisors
ALTER TABLE tcep_form
    ADD CONSTRAINT fk_tcep_form_start_advisor
        FOREIGN KEY (StartAdvisorID) REFERENCES advisor(AdvisorID)
        ON DELETE SET NULL,
    ADD CONSTRAINT fk_tcep_form_current_advisor
        FOREIGN KEY (CurrentAdvisorID) REFERENCES advisor(AdvisorID)
        ON DELETE SET NULL;


-- backfill existing forms based on the student’s advisor
-- This ensures that all existing forms have valid advisor ownership
UPDATE tcep_form f
JOIN student s ON s.StudentID = f.StudentID
SET f.StartAdvisorID   = s.AdvisorID,
    f.CurrentAdvisorID = s.AdvisorID
WHERE f.StartAdvisorID IS NULL
  AND f.CurrentAdvisorID IS NULL;
