/***********************************************************************************************************************
 * JavaFX controller that handles the view of the table of forms
 * Allows opening of forms and quick actions from the table view
***********************************************************************************************************************/

package utd.tcep.controllers;

import java.time.LocalDate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import utd.tcep.data.TCEPForm;
import utd.tcep.data.TCEPFormTable;
import utd.tcep.events.NavigationRequestEvent;

public class FormTableController {

    // from FXML
    @FXML public TableView<TCEPForm> formTable;
    @FXML private TableColumn<TCEPForm, String> studentNameCol;
    @FXML private TableColumn<TCEPForm, String> utdIdCol;
    @FXML private TableColumn<TCEPForm, String> netIdCol;
    @FXML private TableColumn<TCEPForm, LocalDate> dateStartedCol;
    @FXML private TableColumn<TCEPForm, String> schoolNameColumn;
    @FXML private TableColumn<TCEPForm, String> statusCol;
    @FXML private Label dbStatus;   // "DB: not tested yet"
    @FXML private TextField searchField;

    private ObservableList<TCEPForm> masterData = FXCollections.observableArrayList();
    private FilteredList<TCEPForm> filteredData;
    private TCEPFormTable formTableObject = new TCEPFormTable();
    private utd.tcep.data.TCEPUser currentUser;

    // Written by Ryan Pham (rkp200003)
    public TCEPFormTable getFormTableObject() {
        return formTableObject;
    }

    public void setCurrentUser(utd.tcep.data.TCEPUser user) {
        this.currentUser = user;
    }

    /**
     * Removes a form from the table view without reloading from database.
     * This is used when a form has been processed and should disappear from the current user's view.
     */
    public void removeFormFromUI(TCEPForm form) {
        formTableObject.rows.remove(form);
        masterData.remove(form);
        System.out.println("Form removed from UI. Remaining forms: " + masterData.size());
    }

    /**
     * Initializes the Form Table View after the FXML is loaded.
     * <p>
     * This method is automatically called by the JavaFX runtime once the
     * corresponding FXML file (formtableview.fxml) is loaded.
     * It binds each TableColumn to the corresponding property in the TCEPForm model
     * using PropertyValueFactory, ensuring data from the database appears in the correct column.
     * It also performs an initial call to loadForms() to populate the table when the scene is first displayed.
     * written by Jeffrey Chou (jxc033200) and Ryan Pham (rkp200003)
     */
    @FXML
    public void initialize() {
        // 1. bind columns to TCEPForm getters
        studentNameCol.setCellValueFactory(cellData -> cellData.getValue().getStudentNameProperty());
        utdIdCol.setCellValueFactory(cellData -> cellData.getValue().getUtdIdProperty());
        netIdCol.setCellValueFactory(cellData -> cellData.getValue().getNetIdProperty());
        dateStartedCol.setCellValueFactory(cellData -> cellData.getValue().getStartedDateProperty());
        schoolNameColumn.setCellValueFactory(cellData -> cellData.getValue().getSchoolNameProperty());
        statusCol.setCellValueFactory(cellData -> cellData.getValue().getStatusProperty());
        
        // 2. bind form table to TCEPForm table
        formTable.setRowFactory(table -> {
            TableRow<TCEPForm> row = new TableRow<>();

            if (row.getItem() != null)
            {
                row.setCursor(Cursor.HAND);
            }

            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && row.getItem() != null)
                {
                    formTable.fireEvent(new NavigationRequestEvent(row.getItem()));
                }
            });

            return row;
        });

        filteredData = new FilteredList<>(masterData, p -> true); 
        formTable.setItems(filteredData);

        // 3. load data from DB (will be filtered by advisor when user logs in)
        // Initial load shows all forms; after login, refreshMasterData() will filter by advisor
        try {
            // Don't load forms here - wait for login to set currentUser
            // formTableObject.loadForms();
            // masterData.clear();
            // masterData.addAll(formTableObject.rows);

            if (dbStatus != null) {
                dbStatus.setText("DB: Ready to load forms");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (dbStatus != null) {
                dbStatus.setText("DB: ❌ " + e.getMessage());
            }
        }
    }

    // Handles refresh button. Calls loadForms to re-query the DB.
    // Written by Jeffrey Chou (jxc033200)
    @FXML
    private void onRefreshClicked() {
        refreshMasterData();
    }

    // Handles search field input to filter the table
    // Written by Davis Huynh (dxh170005)
    @FXML
    private void onSearchChanged() {
        String search = searchField.getText().toLowerCase();

        if (filteredData != null) {
            filteredData.setPredicate(f -> {
                if (search == null || search.isEmpty()) return true;
                return (f.getStudentName() != null && f.getStudentName().toLowerCase().contains(search))
                        || (f.getUtdId() != null && f.getUtdId().toLowerCase().contains(search))
                        || (f.getNetId() != null && f.getNetId().toLowerCase().contains(search))
                        || (f.getSchoolName() != null && f.getSchoolName().toLowerCase().contains(search));
            });
        }
    }

    public void refreshMasterData() {
        try {
            Integer advisorId = (currentUser != null) ? currentUser.getAdvisorId() : null;
            formTableObject.loadForms(advisorId);
            masterData.clear();
            masterData.addAll(formTableObject.rows);
            
            if (dbStatus != null) {
                String userInfo = (advisorId != null) ? " for advisor " + advisorId : " (all)";
                dbStatus.setText("DB: ✅ loaded " + formTableObject.rows.size() + " form(s)" + userInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (dbStatus != null) {
                dbStatus.setText("DB: ❌ " + e.getMessage());
            }
        }
    }
}
 