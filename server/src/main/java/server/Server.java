package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import message.Message;

public class Server {

  private static final int PORT = 8189;

  private AuthService authService;
  private HashMap<String, ClientHandler> clients;

  public Server() {
    try (ServerSocket server = new ServerSocket(PORT)) {
      clients = new HashMap<>();
      authService = new AuthService();
      authService.start();
      while (true) {
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

  public void broadcastMessage(Message message) {
    for (HashMap.Entry<String, ClientHandler> entry : clients.entrySet()) {
      entry.getValue().send(message);
    }
  }
}
