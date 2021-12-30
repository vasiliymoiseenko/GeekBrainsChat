package client;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class ChatController implements Initializable {

  private Listener listener;

  @FXML TextField login;
  @FXML PasswordField password;
  @FXML HBox chatPane;
  @FXML TextArea chat;
  @FXML TextField message;

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

  public void changeStageToChat() {
    chatPane.setVisible(true);
  }

  public void sendMessage(ActionEvent actionEvent) {
    listener.send(message.getText());
    message.clear();
  }
}
