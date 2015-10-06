package cma.carmelo.jasperviewerfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("JasperViewerFX - Tutor de Programación");
        primaryStage.setScene(new Scene(root, 600, 400));

        Controller ctrl = loader.getController();
        ctrl.setStage(primaryStage);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
