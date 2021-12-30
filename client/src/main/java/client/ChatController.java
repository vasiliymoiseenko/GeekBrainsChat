package client;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ChatController implements Initializable {

  private Listener listener;

  @FXML
  TextField login;

  @FXML
  PasswordField password;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    listener = new Listener(this);
    new Thread(listener).start();
  }

  public void enterChat(ActionEvent event) {
    listener.send("/auth " + login.getText() + " " + password.getText());
    login.clear();
    password.clear();
  }
}
