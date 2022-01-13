package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import message.Bubble;
import message.Message;
import message.Message.MessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientConnection implements Runnable {

  private static final Logger LOGGER = LogManager.getLogger(ClientConnection.class);
  private static final String SERVER = "localhost";
  private static final int PORT = 8189;
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private ChatController controller;
  private String name;

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
        controller.regMessage.setTextFill(Color.GREEN);
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
    name = message.getName();
    LOGGER.info("Authorization completed");
    Platform.runLater(() -> controller.changeStageToChat());
  }

  private void readMessage() throws IOException, ClassNotFoundException {
    while (socket.isConnected()) {
      Message message = (Message) in.readObject();
      switch (message.getMessageType()) {
        case USER -> addAsUser(message);
        case SERVER -> addAsServer(message);
      }
    }
  }

  private void addAsServer(Message message) {
    LOGGER.info(message.getName() + message.getText());
    String text  = "* " + message.getName() + message.getText();
    Bubble chatMessage = new Bubble(text);
    chatMessage.setBubbleColor(Color.GREY);
    GridPane.setHalignment(chatMessage, HPos.CENTER);
    Platform.runLater(() -> controller.chat.addRow(controller.chat.getRowCount(), chatMessage));
  }

  private void addAsUser(Message message) {
    LOGGER.info(message.getName() + ": " + message.getText());
    Bubble chatMessage;
    if (message.getName().equals(name)) {
      chatMessage = new Bubble(message.getText(), "11:20");
      GridPane.setHalignment(chatMessage, HPos.RIGHT);
    } else {
      chatMessage = new Bubble(message.getName() + "\n" + message.getText());
      GridPane.setHalignment(chatMessage, HPos.LEFT);
    }
    Platform.runLater(() -> controller.chat.addRow(controller.chat.getRowCount(), chatMessage));
  }

}
