package it.umarov.cloud.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private Network net;


    @Override
    public void start(Stage loginStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        Parent root = (Parent)loader.load();
        LoginController loginController = (LoginController)loader.getController();
        net = loginController.getNet();
        loginStage.setTitle("login to eddy");
        loginStage.setScene(new Scene(root, 400, 300));
        loginStage.show();
    }

    @Override
    public void stop() throws Exception {
        net.disconnect();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
