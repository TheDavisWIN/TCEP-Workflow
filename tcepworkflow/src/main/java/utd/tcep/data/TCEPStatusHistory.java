package utd.tcep.data;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class TCEPStatusHistory {
    private final ObjectProperty<LocalDateTime> changedOn = new SimpleObjectProperty<>();
    private final StringProperty comments = new SimpleStringProperty();
    private final StringProperty advisorName = new SimpleStringProperty();

    public TCEPStatusHistory(LocalDateTime changedOn, String comments, String advisorName) {
        this.changedOn.set(changedOn);
        this.comments.set(comments != null ? comments : "No comment");
        this.advisorName.set(advisorName != null ? advisorName : "Unknown");
    }

    public LocalDateTime getChangedOn() { return changedOn.get(); }
    public ObjectProperty<LocalDateTime> changedOnProperty() { return changedOn; }

    public String getComments() { return comments.get(); }
    public StringProperty commentsProperty() { return comments; }

    public String getAdvisorName() { return advisorName.get(); }
    public StringProperty advisorNameProperty() { return advisorName; }
}