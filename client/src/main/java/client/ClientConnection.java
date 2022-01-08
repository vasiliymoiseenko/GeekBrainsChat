package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import message.Message;
import message.Message.MessageType;

public class ClientConnection implements Runnable {

  private static final String SERVER = "localhost";
  private static final int PORT = 8189;
  private Socket socket;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private ChatController controller;

  public ClientConnection(ChatController controller) {
    this.controller = controller;
  }

  @Override
  public void run() {
    try {
      socket = new Socket(SERVER, PORT);
      out = new ObjectOutputStream(socket.getOutputStream());
      in = new ObjectInputStream(socket.getInputStream());
      while (socket.isConnected()) {
        authorization();
        readMessage();
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public void send(Message message) throws IOException {
    out.writeObject(message);
    out.reset();
    System.out.println(message);
  }

  private void authorization() throws IOException, ClassNotFoundException {
    while (true) {
      Message message = (Message) in.readObject();
      if (message.getMessageType() == MessageType.CONNECT) {
        System.out.println("connected");
        controller.changeStageToChat();
        break;
      }
    }
  }

  private void readMessage() throws IOException, ClassNotFoundException {
    while (true) {
      Message message = (Message) in.readObject();
      switch (message.getMessageType()) {
        case USER -> addAsUser(message);
        case SERVER -> addAsServer(message);
      }
    }
  }

  private void addAsServer(Message message) {
    String text  = "* " + message.getName() + message.getText() + "\n";
    controller.chat.appendText(text);
  }

  private void addAsUser(Message message) {
    String text = message.getName() + ": " + message.getText() + "\n";
    controller.chat.appendText(text);
  }
}
