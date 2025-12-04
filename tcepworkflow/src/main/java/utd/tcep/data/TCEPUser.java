/***********************************************************************************************************************
 * Data object that holds Users and manages the current session
 * Davis Huynh (dxh170005)
***********************************************************************************************************************/

package utd.tcep.data;

// Written by Davis Huynh (dxh170005)
public class TCEPUser {
    private static TCEPUser currentUser = null;
    
    private String username;
    private Integer advisorId;
    private String advisorName;

    private TCEPUser() {}
    
    private TCEPUser(String username, Integer advisorId, String advisorName) { 
        this.username = username;
        this.advisorId = advisorId;
        this.advisorName = advisorName;
    }

    public static TCEPUser getCurrentUser() {
        return currentUser;
    }
    
    public static void setCurrentUser(String username, Integer advisorId, String advisorName) {
        currentUser = new TCEPUser(username, advisorId, advisorName);
    }
    
    public static void clearCurrentUser() {
        currentUser = null;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public Integer getAdvisorId() { return advisorId; }
    public void setAdvisorId(Integer advisorId) { this.advisorId = advisorId; }
    
    public String getAdvisorName() { return advisorName; }
    public void setAdvisorName(String advisorName) { this.advisorName = advisorName; }
}