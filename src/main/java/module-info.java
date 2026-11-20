module com.example.otelotomasyon {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.logging;
    requires org.jetbrains.annotations;

    opens com.example.otelotomasyon to javafx.fxml;
    exports com.example.otelotomasyon;
}