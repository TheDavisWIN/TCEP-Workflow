--This project was possible by using VScode, javaFX, Maven, and XAMMP

--Accessing phpMyAdmin through XAMPP

- Download the XAMPP installer and follow the steps to Install XAMPP
- Once installed, open the XAMPP Control Panel
- Click on “Start” for the “Apache” module and the “MySQL” module
- Click on “Admin” for the “MySQL” module, a new localhost tab should open
- You now have access to phpMyAdmin

--Setting up the database

- Download the “tcep.sql” file from the Github repository
- In your localhost tab, click “Databases” in the top navigation bar
- In the box that says “Create database”, enter “tcep” into the text field, then click “Create”
- A new database has been created, make sure it is selected in the left panel
- Click on “Import” in the top navigation bar
- In the box that says “File to import:”, choose the “tcep.sql” file you downloaded
- Scroll down to the box that says “Other options” and disable the option that says “Enable foreign key checks”
- All the way at the bottom is the “Import” button, click on it
- If you scroll up, you’ll see text in the “File to import:” box that says "Please be patient, the file is being uploaded.” Wait for the file to import
- If you were successful, there will be a message that says “Import has been successfully finished, 370 queries executed. (tcep.sql)”
- The most recent version of the database is now set up


--vscode extensions

- Maven for Java
- Project Manager for Java
- JavaFX Support
- JavaFX CSS Support
- Extension Pack for Java
- Debugger for Java

--After extracting the ZIP file change directory in the terminal to tcepworkflow "cd tcepworkflow" 
- To run the code run this in the terminal "mvn clean javafx:run" and the application will start prompting for an advisor to log in
- Log in using "jxd123456"
- Once in the TCEP application you may check on students, create TCEP forms, and check a students transfer equivalency
