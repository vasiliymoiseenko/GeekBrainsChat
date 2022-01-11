package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javafx.application.Platform;
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
      if (message.getMessageType() == MessageType.AUTH) {
        LOGGER.warn(message.getText());
        Platform.runLater(() -> {
          controller.authError.setText(message.getText());
          controller.authError.setVisible(true);
        });
      } else if (message.getMessageType() == MessageType.CONNECT) {
        LOGGER.info("Authorization completed");
        Platform.runLater(() -> controller.changeStageToChat());
        break;
      }
    }
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
    String text  = "* " + message.getName() + message.getText() + "\n";
    controller.chat.appendText(text);
  }

  private void addAsUser(Message message) {
    LOGGER.info(message.getName() + ": " + message.getText());
    String text = message.getName() + ": " + message.getText() + "\n";
    controller.chat.appendText(text);
  }
}
