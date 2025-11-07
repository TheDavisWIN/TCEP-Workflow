/***********************************************************************************************************************
 * Data object that holds fields found in a TCEP form
***********************************************************************************************************************/

package utd.tcep.data;

import java.time.LocalDate;

public class TCEPForm {
    
    public enum Term {
        Fall,
        Spring,
        Summer,
        Other
    }

    public LocalDate requestDate;
    public Term term;
    public int year;
    public String degreeRequirement;
    public String coreDesignation;
    public int incomingCourseID;
    public int equivalentCourseID;
    public int institutionID;

    //Form Table View fields
    private String studentName;   
    private String utdId;         
    private String netId;       
    private LocalDate startedDate;
    private String status;

    /**
     * Getters and setters for table view fields
     * Written by Jeffrey Chou (jxc033200)
     */
    // --- getters (required by TableView) ---
    public String getStudentName() { return studentName; }
    public String getUtdId()       { return utdId; }
    public String getNetId()       { return netId; }
    public LocalDate getStartedDate() { return startedDate; }
    public String getStatus()      { return status; }

    // --- setters (used by controller when mapping ResultSet) ---
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setUtdId(String utdId)             { this.utdId = utdId; }
    public void setNetId(String netId)             { this.netId = netId; }
    public void setStartedDate(LocalDate startedDate) { this.startedDate = startedDate; }
    public void setStatus(String status)           { this.status = status; }

}
