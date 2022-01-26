package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import message.Message;
import message.Message.MessageType;
import message.UserCell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {

  private static final int PORT = 8189;
  private static final Logger LOGGER = LogManager.getLogger(Server.class);
  private final ExecutorService threadPool = Executors.newCachedThreadPool();

  private static AuthService authService = new AuthService();
  private static ArrayList<ClientHandler> clients = new ArrayList<>();
  private static HashMap<String, UserCell> userList = new HashMap<>();

  public Server() {
    try (ServerSocket server = new ServerSocket(PORT)) {
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
    return userList.containsKey(login);
  }

  public synchronized void addClient(ClientHandler ch) {
    userList.put(ch.getLogin(), new UserCell(ch.getName(), "..."));
    clients.add(ch);
    broadcastClients();
  }

  public synchronized void removeClient(ClientHandler ch) {
    userList.remove(ch.getLogin());
    clients.remove(ch);
    broadcastClients();
  }

  private void broadcastClients() {
    Message message = new Message();
    message.setMessageType(MessageType.LIST);
    message.setUserList(userList);
    broadcastMessage(message);
  }

  private void shutdown() {
    if (authService != null) {
      authService.stop();
    }
    threadPool.shutdownNow();
    LOGGER.info("Server is offline");
  }

  public void broadcastMessage(Message message) {
    for (ClientHandler ch: clients) {
      ch.send(message);
    }
  }

  public void changeStatus(String login, String text) {
    userList.get(login).setStatus(text);
    broadcastClients();
  }
}
