module utdtcep {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens utd.tcep.main to javafx.fxml;
    opens utd.tcep to javafx.fxml;
    exports utd.tcep;
    exports utd.tcep.main;
}
