/***********************************************************************************************************************
 * Data object that holds fields found in a TCEP form
***********************************************************************************************************************/

package utd.tcep.data;

import java.time.LocalDate;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

public class TCEPForm {
    
    public enum Term {
        Fall,
        Spring,
        Summer,
        Other
    }

    public final int formID;
    public TCEPStatusHistory tcepStatusHistory;
    public LocalDate requestDate;
    public Term term;
    public int year;
    public String degreeRequirement;
    public String coreDesignation;
    public int incomingCourseID;
    public int equivalentCourseID;
    public int institutionID;

    //Form Table View fields
    private SimpleStringProperty studentName;   
    private SimpleStringProperty utdId;         
    private SimpleStringProperty netId;       
    private SimpleObjectProperty<LocalDate> startedDate;
    private SimpleStringProperty schoolName;
    private SimpleStringProperty status;

    // Constructor
    // Written by Ryan Pham (rkp200003)
    public TCEPForm(int formID)
    {
        this.formID = formID;
        this.studentName = new SimpleStringProperty();
        this.utdId = new SimpleStringProperty();
        this.netId = new SimpleStringProperty();
        this.startedDate = new SimpleObjectProperty<LocalDate>();
        this.schoolName = new SimpleStringProperty();
        this.status = new SimpleStringProperty();
    }

    /**
     * Getters and setters for table view fields
     * Written by Jeffrey Chou (jxc033200)
     * and Ryan Pham (rkp200003); changed to use JavaFX properties and added Property getters
     */
    // --- getters (required by TableView) ---
    public int getId()                  { return formID; }
    public String getStudentName()      { return studentName.get(); }
    public String getUtdId()            { return utdId.get(); }
    public String getNetId()            { return netId.get(); }
    public LocalDate getStartedDate()   { return startedDate.get(); }
    public String getSchoolName()       { return schoolName.get(); }
    public String getStatus()           { return status.get(); }
    public String getDegReq()           { return degreeRequirement; }
    public String getCoreDes()          { return coreDesignation; }
    public int getIncomingID()          { return incomingCourseID; }
    public int getEquivalentID()        { return equivalentCourseID; }
    public int getInstitutionID()       { return institutionID; }

    // --- property getters ---
    public SimpleStringProperty getStudentNameProperty()      { return studentName; }
    public SimpleStringProperty getUtdIdProperty()            { return utdId; }
    public SimpleStringProperty getNetIdProperty()            { return netId; }
    public SimpleObjectProperty<LocalDate> getStartedDateProperty()   { return startedDate; }
    public SimpleStringProperty getSchoolNameProperty()       { return schoolName; }
    public SimpleStringProperty getStatusProperty()           { return status; }

    // --- setters (used by controller when mapping ResultSet) ---
    public void setStudentName(String studentName)         { this.studentName.set(studentName); }
    public void setUtdId(String utdId)                     { this.utdId.set(utdId); }
    public void setNetId(String netId)                     { this.netId.set(netId); }
    public void setStartedDate(LocalDate startedDate)      { this.startedDate.set(startedDate); }
    public void setSchoolName(String schoolName)           { this.schoolName.set(schoolName); }
    public void setStatus(String status)                   { this.status.set(status); }

    public void setDegReq(String degreeRequirement)        { this.degreeRequirement = degreeRequirement; }
    public void setCoreDes(String coreDesignation)         { this.coreDesignation = coreDesignation; }
    public void setIncomingID(int incomingCourseID)        { this.incomingCourseID = incomingCourseID; }
    public void setEquivalentID(int equivalentCourseID)    { this.equivalentCourseID = equivalentCourseID; }
    public void setInstitutionID(int institutionID)        { this.institutionID = institutionID; }

}
