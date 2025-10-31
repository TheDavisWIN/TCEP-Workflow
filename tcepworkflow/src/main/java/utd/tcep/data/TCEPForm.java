/***********************************************************************************************************************
 * Data object that holds fields found in a TCEP form
***********************************************************************************************************************/

package utd.tcep.data;

import java.util.Date;

public class TCEPForm {
    
    enum Term {
        Fall,
        Spring,
        Summer,
        Other
    }

    public int studentID;
    public Date requestDate;
    public Term term;
    public int year;
    public String degreeRequirement;
    public String coreDesignation;
    public int incomingCourseID;
    public int equivalentCourseID;
    public int institutionID;

}
