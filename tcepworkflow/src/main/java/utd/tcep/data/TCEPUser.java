/***********************************************************************************************************************
 * Data object that holds Users and connects with the database service to populate the table
 * Davis Huynh (dxh170005)
***********************************************************************************************************************/

package utd.tcep.data;

// Written by Davis Huynh (dxh170005)
public class TCEPUser {
    private String username;

    public TCEPUser() {}
    public TCEPUser(String username) { this.username = username; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}