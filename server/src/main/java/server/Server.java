package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server {

  private static final int PORT = 8189;

  private AuthService authService;
  private HashMap<String, ClientHandler> clients;

  public Server() {
    try (ServerSocket server = new ServerSocket(PORT)) {
      authService = new AuthService();
      clients = new HashMap<>();
      authService.start();
      while (true) {
        System.out.println("Server is waiting for clients...");
        Socket socket = server.accept();
        System.out.println("Client is connected");
        new ClientHandler(this, socket);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      shutdown();
    }
  }

  public static void main(String[] args) {
    new Server();
  }

  public AuthService getAuthService() {
    return authService;
  }

  public synchronized boolean isOnline(String login) {
    return clients.containsKey(login);
  }

  public synchronized void addClient(String login, ClientHandler ch) {
    clients.put(login, ch);
  }

  public synchronized void removeClient(String login) {
    clients.remove(login);
  }

  private void shutdown() {
    if (authService != null) {
      authService.stop();
    }
    System.out.println("Server is offline");
  }

  public void sendMessage(String message) {
    for (HashMap.Entry<String, ClientHandler> entry : clients.entrySet()) {
      entry.getValue().sendMessage(message);
    }
  }
}
