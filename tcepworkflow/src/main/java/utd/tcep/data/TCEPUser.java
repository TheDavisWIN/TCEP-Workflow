/***********************************************************************************************************************
 * Data object that holds Users and connects with the database service to populate the table
 * Davis Huynh (dxh170005)
***********************************************************************************************************************/

package utd.tcep.data;

// Written by Davis Huynh (dxh170005)
public class TCEPUser {
    private String username;
    private Integer advisorId;

    public TCEPUser() {}
    public TCEPUser(String username) { this.username = username; }
    public TCEPUser(String username, Integer advisorId) { 
        this.username = username; 
        this.advisorId = advisorId;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getAdvisorId() { return advisorId; }
    public void setAdvisorId(Integer advisorId) { this.advisorId = advisorId; }
}