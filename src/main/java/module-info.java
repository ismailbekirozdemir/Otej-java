module com.example.otelotomasyon {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.jetbrains.annotations;
    requires java.desktop;
    requires jdk.jdi;

    opens com.example.otelotomasyon to javafx.fxml;
    exports com.example.otelotomasyon;
}