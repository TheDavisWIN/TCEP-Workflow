/***********************************************************************************************************************
 * Data object that holds TCEP forms and connects with the database service to populate the table
***********************************************************************************************************************/

package utd.tcep.data;

import java.sql.SQLException;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utd.tcep.db.TCEPDatabaseService;

public class TCEPFormTable {
    public ObservableList<TCEPForm> rows = FXCollections.observableArrayList();

    /**
     * Retrieves form records from the MySQL database and populates the TableView.
     * Executes a SQL SELECT query on the TCEP_Form table (and related tables in the future).
     * Each row from the ResultSet is converted into a TCEPForm object and added to an ObservableList,
     * which is then bound to the TableView for display.
     * written by Jeffrey Chou (jxc033200)
     */
    public void loadForms() {
        rows = TCEPDatabaseService.getFormsFromDB();
    }

    public TCEPForm createBlankForm() {
        TCEPForm newForm = new TCEPForm(getNewFormID());
        newForm.setStartedDate(LocalDate.now());
        rows.add(newForm);
        return newForm;
    }

    public int getNewFormID() {
        int maxID = 0;
        for (TCEPForm form : rows) {
            if (form.formID > maxID) {
                maxID = form.formID;
            }
        }
        return maxID + 1;
    }
}