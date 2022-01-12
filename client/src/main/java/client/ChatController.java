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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import message.Message;
import message.Message.MessageType;

public class ChatController implements Initializable {

  private ClientConnection connection;

  @FXML GridPane authPane;
  @FXML TextField authLogin;
  @FXML PasswordField authPassword;
  @FXML Label authMessage;

  @FXML GridPane regPane;
  @FXML TextField regLogin;
  @FXML PasswordField regPassword;
  @FXML TextField regName;
  @FXML Label regMessage;

  @FXML HBox chatPane;
  @FXML TextArea chat;
  @FXML TextField messageField;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    connection = new ClientConnection(this);
    new Thread(connection).start();
  }

  public void sendMessage(ActionEvent actionEvent) throws IOException {
    Message message = new Message();
    message.setMessageType(MessageType.USER);
    message.setText(messageField.getText());
    connection.send(message);
    messageField.clear();
  }

  public void enterChat(ActionEvent event) throws IOException {
    if (authLogin.getText().isEmpty() || authPassword.getText().isEmpty()) {
      authMessage.setText("Enter login and password");
      authMessage.setVisible(true);
    } else {
      Message message = new Message();
      message.setMessageType(MessageType.AUTH);
      message.setLogin(authLogin.getText());
      message.setPassword(authPassword.getText());
      connection.send(message);
    }
    /*authLogin.clear();
    authPassword.clear();*/
  }

  public void changeStageToChat() {
    authPane.setVisible(false);
    regPane.setVisible(false);
    chatPane.setVisible(true);
  }

  public void changeStageToReg(ActionEvent event) {
    authPane.setVisible(false);
    regPane.setVisible(true);
    chatPane.setVisible(false);
  }

  public void changeStageToAuth(ActionEvent event) {
    authPane.setVisible(true);
    regPane.setVisible(false);
    chatPane.setVisible(false);
  }

  public void register() throws IOException {
    if (regLogin.getText().isEmpty() || regPassword.getText().isEmpty() || regName.getText().isEmpty()) {
      regMessage.setTextFill(Color.RED);
      regMessage.setText("Enter login, password and name");
      regMessage.setVisible(true);
    } else {
      Message message = new Message();
      message.setMessageType(MessageType.REG);
      message.setLogin(regLogin.getText());
      message.setPassword(regPassword.getText());
      message.setName(regName.getText());
      connection.send(message);
    }
    /*regLogin.clear();
    regPassword.clear();
    regName.clear();*/
  }


}
