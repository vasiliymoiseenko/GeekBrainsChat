package client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import message.Message;
import message.Message.MessageType;

public class ChatController implements Initializable {

  private ClientConnection connection;

  @FXML TextField loginField;
  @FXML PasswordField passwordField;
  @FXML HBox chatPane;
  @FXML TextArea chat;
  @FXML TextField messageField;
  @FXML Label authError;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    connection = new ClientConnection(this);
    new Thread(connection).start();
  }

  public void enterChat(ActionEvent event) throws IOException {
    if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
      authError.setText("Enter login and password");
      authError.setVisible(true);
    } else {
      Message message = new Message();
      message.setMessageType(MessageType.AUTH);
      message.setLogin(loginField.getText());
      message.setPassword(passwordField.getText());
      connection.send(message);
    }
    loginField.clear();
    passwordField.clear();
  }

  public void changeStageToChat() {
    chatPane.setVisible(true);
  }

  public void sendMessage(ActionEvent actionEvent) throws IOException {
    Message message = new Message();
    message.setMessageType(MessageType.USER);
    message.setText(messageField.getText());
    connection.send(message);
    messageField.clear();
  }
}
