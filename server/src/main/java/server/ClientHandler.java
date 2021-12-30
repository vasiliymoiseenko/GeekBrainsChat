package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {

  private static ExecutorService threadPool = Executors.newCachedThreadPool();
  private Server server;
  private Socket socket;
  private AuthService authService;
  private DataInputStream in;
  private DataOutputStream out;
  private String name;
  private String login;

  ClientHandler(Server server, Socket socket) {
    try {
      this.server = server;
      this.socket = socket;
      this.authService = server.getAuthService();
      this.in = new DataInputStream(socket.getInputStream());
      this.out = new DataOutputStream(socket.getOutputStream());

      threadPool.execute(() -> {
        try {
          authentication();
          readMessage();
        } catch (IOException e) {
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

  private void readMessage() throws IOException {
    while (true) {
      String strFromClient = in.readUTF();
      System.out.println("from " + name + ": " + strFromClient);
      server.sendMessage(name + ": " + strFromClient);
    }
  }

  private void authentication() throws IOException {
    while (true) {
      String message = in.readUTF();
      System.out.println(message);
      if (message.startsWith("/auth")) {
        String[] parts = message.split("\\s");
        String nameByLoginPass = server.getAuthService().getNameByLoginPass(parts[1], parts[2]);
        if (nameByLoginPass != null) {
          if (!server.isOnline(parts[1])) {
            login = parts[1];
            name = nameByLoginPass;
            System.out.println("/authok " + login);
            sendMessage("/authok " + login);
            server.sendMessage(name + " entered the chat");
            System.out.println(name + " entered the chat");
            server.addClient(login, this);
            return;
          } else {
            sendMessage("Account is already online");
          }
        } else {
          sendMessage("Invalid username / password");
        }
      }
    }
  }

  public void sendMessage(String message) {
    try {
      out.writeUTF(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void closeConnection() {
    if (login != null) {
      server.removeClient(login);
      server.sendMessage(name + " left the chat");
      System.out.println(name + " left the chat");
    }
    try {
      in.close();
      out.close();
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
