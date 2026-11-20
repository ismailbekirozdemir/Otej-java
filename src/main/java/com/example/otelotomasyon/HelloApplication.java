package com.example.otelotomasyon;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage)throws IOException{

        String path = "/com/example/otelotomasyon/main-view.fxml";

        URL url = Main.class.getResource(path);

        if (url == null) {
            throw new RuntimeException("FXML DOSYASI BULUNAMADI!");
        }

        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();

        Scene scene = new Scene(root, 1400, 860);
        URL cssUrl = getClass().getResource("/com/example/otelotomasyon/app-theme.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setScene(scene);
        stage.setTitle("Otel Otomasyon • Resepsiyon & Yönetim Sistemi");

        try{
            Image icon = new Image(
                    getClass().getResourceAsStream("/hotel-icon.png")
            );
            stage.getIcons().add(icon);
        }catch (NullPointerException e){
            System.out.println(e.getMessage());
        }
        stage.setFullScreen(true);
        stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("A"));
        stage.setMinWidth(1024);
        stage.setMinHeight(680);

        stage.show();
    }
}
