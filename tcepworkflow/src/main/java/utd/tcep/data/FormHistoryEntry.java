package utd.tcep.data;

import javafx.beans.property.SimpleStringProperty;

public class FormHistoryEntry {
    private final SimpleStringProperty date;
    private final SimpleStringProperty action;
    private final SimpleStringProperty reviewer;

    public FormHistoryEntry(String date, String action, String reviewer) {
        this.date = new SimpleStringProperty(date);
        this.action = new SimpleStringProperty(action);
        this.reviewer = new SimpleStringProperty(reviewer != null ? reviewer : "System");
    }

    public String getDate() { return date.get(); }
    public SimpleStringProperty dateProperty() { return date; }

    public String getAction() { return action.get(); }
    public SimpleStringProperty actionProperty() { return action; }

    public String getReviewer() { return reviewer.get(); }
    public SimpleStringProperty reviewerProperty() { return reviewer; }
}
