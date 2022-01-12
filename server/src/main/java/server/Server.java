package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import message.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {

  private static final int PORT = 8189;
  private static final Logger LOGGER = LogManager.getLogger(Server.class);
  private final ExecutorService threadPool = Executors.newCachedThreadPool();

  private AuthService authService;
  private HashMap<String, ClientHandler> clients;

  public Server() {
    try (ServerSocket server = new ServerSocket(PORT)) {
      clients = new HashMap<>();
      authService = new AuthService();
      authService.start();
      while (true) {
        Socket socket = server.accept();
        LOGGER.info("Client is connected");
        new ClientHandler(this, socket, threadPool);
      }
    } catch (IOException e) {
      LOGGER.error(e);
      LOGGER.debug(e.toString(), e);
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
    threadPool.shutdownNow();
    LOGGER.info("Server is offline");
  }

  public void broadcastMessage(Message message) {
    for (HashMap.Entry<String, ClientHandler> entry : clients.entrySet()) {
      entry.getValue().send(message);
    }
  }
}
