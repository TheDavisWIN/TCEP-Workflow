--Ayden Benel
--Need to add NetID to the database since last time Student_ID was not enough, forgot there was a NetID and UtdID
--this is meant to be implemented after tcep.sql file

--Adding the NetID, they are all unique no one has the same NetID
ALTER TABLE student
ADD COLUMN NetID VARCHAR(20) NOT NULL UNIQUE;

--Add NetID to tcep_form
ALTER TABLE tcep_form
ADD COLUMN NetID VARCHAR(20),
ADD CONSTRAINT fk_tcep_form_netid--this is the foreign key name for this table, so I can reference it much easier
  FOREIGN KEY (NetID) REFERENCES student(NetID)--the contraint is NetID, and it references student NetID, Tcep_form NetID must match the student NetID
  ON DELETE SET NULL;

--NetID for tcep_status_history, will refererence Student NetID
ALTER TABLE tcep_status_history
ADD COLUMN NetID VARCHAR(20),
ADD CONSTRAINT fk_tcep_status_history_netid
  FOREIGN KEY (NetID) REFERENCES student(NetID)
  ON DELETE SET NULL;

--Add NetID to transfers, I cant insert a NetID to transfer that doesnt belong to student NetID, this goes for all the tables above
ALTER TABLE transfers
ADD COLUMN NetID VARCHAR(20),
ADD CONSTRAINT fk_transfers_netid
  FOREIGN KEY (NetID) REFERENCES student(NetID)
  ON DELETE SET NULL;
