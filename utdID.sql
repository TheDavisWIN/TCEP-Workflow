--Davis Huynh
--add UtdID to student table and other tables that will reference student UtdID

--Adding the UtdID, they are all unique no one has the same UtdID
ALTER TABLE student
ADD COLUMN UtdID INT(10) NOT NULL UNIQUE;

--Add UtdID to tcep_form
ALTER TABLE tcep_form
ADD COLUMN UtdID INT(10),
ADD CONSTRAINT fk_tcep_form_utdid--this is the foreign key name for this table, so I can reference it much easier
  FOREIGN KEY (UtdID) REFERENCES student(UtdID)--the contraint is UtdID, and it references student UtdID, Tcep_form UtdID must match the student UtdID
  ON DELETE SET NULL;

--UtdID for tcep_status_history, will refererence Student UtdID
ALTER TABLE tcep_status_history
ADD COLUMN UtdID INT(10),
ADD CONSTRAINT fk_tcep_status_history_utdid
  FOREIGN KEY (UtdID) REFERENCES student(UtdID)
  ON DELETE SET NULL;

--Add UtdID to transfers, I cant insert a UtdID to transfer that doesnt belong to student UtdID, this goes for all the tables above
ALTER TABLE transfers
ADD COLUMN UtdID INT(10),
ADD CONSTRAINT fk_transfers_utdid
  FOREIGN KEY (UtdID) REFERENCES student(UtdID)
  ON DELETE SET NULL;