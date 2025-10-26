module utdtcep {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens utd.tcep.main to javafx.fxml;
    opens utd.tcep.controllers to javafx.fxml;

    exports utd.tcep.main;
}
