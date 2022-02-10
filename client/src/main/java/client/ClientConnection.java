package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import message.UserCell;
import message.UserPicture;
import message.Bubble;
import message.Message;
import message.Message.MessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientConnection implements Runnable {

  private static final Logger LOGGER = LogManager.getLogger(ClientConnection.class);
  private static final String SERVER = "localhost";
  private static final int PORT = 8189;
  private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("HH:mm");
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private ChatController controller;
  private String login;

  public ClientConnection(ChatController controller) {
    try {
      this.controller = controller;
      socket = new Socket(SERVER, PORT);
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());
    } catch (IOException e) {
      LOGGER.error(e);
      LOGGER.debug(e.toString(), e);
    }
  }

  @Override
  public void run() {
    try {
      while (socket.isConnected()) {
        authorization();
        readMessage();
      }
    } catch (IOException | ClassNotFoundException e) {
      LOGGER.error(e);
      LOGGER.debug(e.toString(), e);
    }
  }

  public void send(Message message) throws IOException {
    out.writeObject(message);
    out.reset();
    LOGGER.debug("SEND: " + message);
  }

  private void authorization() throws IOException, ClassNotFoundException {
    while (socket.isConnected()) {
      Message message = (Message) in.readObject();
      if (message.getMessageType() == MessageType.REG) {
        displayRegMessage(message);
      }
      if (message.getMessageType() == MessageType.AUTH) {
        displayAuthMessage(message);
      } else if (message.getMessageType() == MessageType.CONNECT) {
        connect(message);
        break;
      }
    }
  }

  private void displayRegMessage(Message message) {
    if (message.getLogin() != null) {
      Platform.runLater(() -> {
        controller.regMessage.setTextFill(Color.ROYALBLUE);
        controller.regMessage.setText("User " + message.getLogin() + " registered");
        controller.regMessage.setVisible(true);
      });
    } else {
      Platform.runLater(() -> {
        controller.regMessage.setTextFill(Color.RED);
        controller.regMessage.setText(message.getText());
        controller.regMessage.setVisible(true);
      });
    }
  }

  private void displayAuthMessage(Message message) {
    LOGGER.warn(message.getText());
    Platform.runLater(() -> {
      controller.authMessage.setText(message.getText());
      controller.authMessage.setVisible(true);
    });
  }

  private void connect(Message message) {
    login = message.getLogin();
    LOGGER.info("Authorization completed");
    Platform.runLater(() -> {
      ArrayList<Message> history = message.getHistory();
      for (Message m: history) {
        switch (m.getMessageType()) {
          case USER -> addAsUser(m);
          case SERVER -> addAsServer(m);
        }
      }
      controller.changeStageToChat();
    });
  }

  private void readMessage() throws IOException, ClassNotFoundException {
    while (socket.isConnected()) {
      Message message = (Message) in.readObject();
      switch (message.getMessageType()) {
        case USER -> addAsUser(message);
        case SERVER -> addAsServer(message);
        case LIST -> updateUserList(message);
        case SET -> updateSettings(message);
      }
    }
  }

  private void updateSettings(Message message) {
    /*if (message.getName() != null) {
      name = message.getName();
    }*/
    if (message.getText() == null) {
      Platform.runLater(() -> {
        controller.setMessage.setTextFill(Color.ROYALBLUE);
        controller.setMessage.setText("Changes saved");
        controller.setMessage.setVisible(true);
      });
    } else {
      Platform.runLater(() -> {
        controller.setMessage.setTextFill(Color.RED);
        controller.setMessage.setText(message.getText());
        controller.setMessage.setVisible(true);
      });
    }
  }

  private void updateUserList(Message message) {
    LOGGER.debug(message);
    Platform.runLater(() -> {
      ObservableList<UserCell> users = FXCollections.observableArrayList(message.getUserList());
      controller.userList.setItems(users);
      controller.userList.setCellFactory(new CellRenderer());
    });
  }

  private void addAsServer(Message message) {
    LOGGER.info(message.getName() + message.getText());
    String text = message.getName() + message.getText();
    Bubble chatMessage = new Bubble(text);
    GridPane.setHalignment(chatMessage, HPos.CENTER);
    Platform.runLater(() -> controller.chat.addRow(controller.chat.getRowCount(), chatMessage));
  }

  private void addAsUser(Message message) {
    LOGGER.info(message.getName() + ": " + message.getText());
    HBox chatMessage = new HBox();
    chatMessage.setSpacing(10);
    if (message.getLogin().equals(login)) {
      chatMessage.setAlignment(Pos.TOP_RIGHT);
      chatMessage.getChildren().add(new Bubble(message.getText(), FORMATTER.format(message.getDate())));
      chatMessage.getChildren().add(new UserPicture());
    } else {
      chatMessage.setAlignment(Pos.TOP_LEFT);
      chatMessage.getChildren().add(new UserPicture());
      chatMessage.getChildren().add(new Bubble(message.getName(), message.getText(), FORMATTER.format(message.getDate())));
    }
    Platform.runLater(() -> controller.chat.addRow(controller.chat.getRowCount(), chatMessage));
  }
}
