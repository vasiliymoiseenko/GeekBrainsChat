package client;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import message.Message;
import message.Message.MessageType;
import message.UserCell;

public class ChatController implements Initializable {

  private ClientConnection connection;

  @FXML GridPane authPane;
  @FXML TextField authLogin;
  @FXML PasswordField authPassword;
  @FXML Label authMessage;

  @FXML GridPane regPane;
  @FXML TextField regLogin;
  @FXML PasswordField regPassword;
  @FXML PasswordField regPasswordRep;
  @FXML TextField regName;
  @FXML Label regMessage;

  @FXML TextField status;
  @FXML ListView<UserCell> userList;
  @FXML ScrollPane scrollPane;
  @FXML HBox chatPane;
  @FXML GridPane chat;
  @FXML TextField messageField;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    changeStageToAuth();

    scrollPane.setFitToWidth(true);
    scrollPane.vvalueProperty().bind(chat.heightProperty());
  }

  public void sendMessage(ActionEvent actionEvent) throws IOException {
    if (!messageField.getText().strip().isEmpty()) {
      Message message = new Message();
      message.setMessageType(MessageType.USER);
      message.setText(messageField.getText().trim());
      connection.send(message);
    }
    messageField.clear();
  }

  public void enterChat(ActionEvent event) throws IOException {
    if (connection == null) {
      connection = new ClientConnection(this);
      new Thread(connection).start();
    }
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
  }

  public void changeStageToChat() {
    authPane.setVisible(false);
    regPane.setVisible(false);
    chatPane.setVisible(true);
  }

  public void changeStageToReg() {
    regLogin.clear();
    regPassword.clear();
    regPasswordRep.clear();
    regName.clear();
    regMessage.setVisible(false);

    authPane.setVisible(false);
    regPane.setVisible(true);
    chatPane.setVisible(false);
  }

  public void changeStageToAuth() {
    authLogin.clear();
    authPassword.clear();
    authMessage.setVisible(false);

    authPane.setVisible(true);
    regPane.setVisible(false);
    chatPane.setVisible(false);
  }

  public void register() throws IOException {
    if (connection == null) {
      connection = new ClientConnection(this);
      new Thread(connection).start();
    }
    if (regLogin.getText().isEmpty() || regPassword.getText().isEmpty()
        || regPasswordRep.getText().isEmpty() || regName.getText().isEmpty()) {
      regMessage.setTextFill(Color.RED);
      regMessage.setText("Enter login, password and name");
      regMessage.setVisible(true);
    } else if (!regPassword.getText().equals(regPasswordRep.getText())) {
      regMessage.setTextFill(Color.RED);
      regMessage.setText("Passwords do not match");
      regMessage.setVisible(true);
    } else {
      Message message = new Message();
      message.setMessageType(MessageType.REG);
      message.setLogin(regLogin.getText());
      message.setPassword(regPassword.getText());
      message.setName(regName.getText());
      connection.send(message);
    }
  }

  public void changeStageToSet(MouseEvent mouseEvent) {
  }

  public void sendStatus() throws IOException{
    if (!status.getText().strip().isEmpty()) {
      Message message = new Message();
      message.setMessageType(MessageType.LIST);
      message.setText(status.getText());
      connection.send(message);
      status.clear();
    }
  }

  public void sendDisconnect(MouseEvent mouseEvent) throws IOException{
    Message message = new Message();
    message.setMessageType(MessageType.DISCONNECT);
    connection.send(message);
    connection = null;
    changeStageToAuth();
  }
}
