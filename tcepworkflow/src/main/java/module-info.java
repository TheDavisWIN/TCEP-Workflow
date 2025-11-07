module utdtcep {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.sql;

    opens utd.tcep.main to javafx.fxml;
    opens utd.tcep.controllers to javafx.fxml;
    opens utd.tcep.data to javafx.fxml;
    opens utd.tcep.db to javafx.fxml;
    exports utd.tcep.main;
    exports utd.tcep.controllers;
    exports utd.tcep.data;
    exports utd.tcep.db;
}
