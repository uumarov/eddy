package it.umarov.cloud.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    private Stage loginStage;
    private Stage primaryStage;
    private Network net;

    public Network getNet() {
        return net;
    }



    @FXML
    TextField loginTextfield, passTextfield;

    @FXML
    Button loginButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            net = new Network();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    public void setStage(Stage primaryStage) {
//        this.primaryStage = primaryStage;
//    }

    public void loginButton(ActionEvent actionEvent) throws IOException {

        if(net.login(loginTextfield.getText(), passTextfield.getText())) {
            showMainController();
            closeLoginForm();
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка аутентификации!", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void showMainController()  throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
        Parent root = (Parent)loader.load();
        primaryStage = new Stage();
        Controller controller = (Controller)loader.getController();
        controller.setStage(primaryStage);
        controller.setNet(net);
        primaryStage.setTitle("eddy");
        primaryStage.setScene(new Scene(root, 1024, 768));
        primaryStage.show();
    }

    private void closeLoginForm() {
        loginStage = (Stage) loginButton.getScene().getWindow();
        loginStage.close();
    }
}
