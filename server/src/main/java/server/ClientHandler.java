package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import message.Message;
import message.Message.MessageType;

public class ClientHandler {

  private final static ExecutorService threadPool = Executors.newCachedThreadPool();
  private final Server server;
  private final Socket socket;
  private final AuthService authService;
  private final ObjectInputStream in;
  private final ObjectOutputStream out;
  private String name;
  private String login;

  ClientHandler(Server server, Socket socket) {
    try {
      this.server = server;
      this.socket = socket;
      this.authService = server.getAuthService();
      this.out = new ObjectOutputStream(socket.getOutputStream());
      this.in = new ObjectInputStream(socket.getInputStream());
      threadPool.execute(() -> {
        try {
          authentication();
          readMessage();
        } catch (IOException | ClassNotFoundException e) {
          e.printStackTrace();
        } finally {
          closeConnection();
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("ClientHandler error");
    }
  }

  private void authentication() throws IOException, ClassNotFoundException {
    while (socket.isConnected()) {
      Message message = (Message) in.readObject();
      System.out.println(message);
      if (message.getMessageType() == MessageType.AUTH) {
        String nameByLoginPass = authService.getNameByLoginPass(message.getLogin(), message.getPassword());
        if (nameByLoginPass != null) {
          if (!server.isOnline(message.getLogin())) {
            login = message.getLogin();
            name = nameByLoginPass;
            completeAuth();
            notifyAboutLogin();
            server.addClient(login, this);
            return;
          } else {
            //sendMessage("Account is already online");
          }
        } else {
          //sendMessage("Invalid username / password");
        }
      }
    }
  }

  private void completeAuth() {
    Message message = new Message();
    message = new Message();
    message.setMessageType(MessageType.CONNECT);
    send(message);
  }

  private void notifyAboutLogin() {
    Message message = new Message();
    message.setMessageType(MessageType.SERVER);
    message.setName(name);
    message.setText(" entered the chat");
    server.broadcastMessage(message);
    System.out.println(name + " entered the chat");
  }

  private void readMessage() throws IOException, ClassNotFoundException {
    while (socket.isConnected()) {
      Message message = (Message) in.readObject();
      System.out.println(message);
      switch (message.getMessageType()) {
        case USER -> broadcastMessage(message);
      }
    }
  }

  private void broadcastMessage(Message message) {
    message.setName(name);
    server.broadcastMessage(message);
  }

  public void send(Message message) {
    try {
      out.writeObject(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void closeConnection() {
    if (login != null) {
      server.removeClient(login);
      notifyAboutExit();

    }
    try {
      in.close();
      out.close();
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void notifyAboutExit() {
    Message message = new Message();
    message.setMessageType(MessageType.SERVER);
    message.setName(name);
    message.setText(" left the chat");
    server.broadcastMessage(message);
    System.out.println(name + " left the chat");
  }
}
