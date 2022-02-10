package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import message.Message;
import message.Message.MessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientHandler {

  private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

  private Server server;
  private Socket socket;
  private AuthService authService;
  private HistoryLogService historyLogService;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private String name;
  private String login;

  ClientHandler(Server server, Socket socket, ExecutorService threadPool) {
    try {
      this.server = server;
      this.socket = socket;
      this.authService = server.getAuthService();
      this.historyLogService = server.getHistoryLogService();
      this.out = new ObjectOutputStream(socket.getOutputStream());
      this.in = new ObjectInputStream(socket.getInputStream());
      threadPool.execute(() -> {
        try {
          authentication();
          readMessage();
        } catch (IOException | ClassNotFoundException | SQLException e) {
          LOGGER.error(e);
          LOGGER.debug(e.toString(), e);
        } finally {
          closeConnection();
        }
      });
    } catch (IOException e) {
      LOGGER.error(e);
      LOGGER.debug(e.toString(), e);
    }
  }

  public String getName() {
    return name;
  }

  public String getLogin() {
    return login;
  }

  private void authentication() throws IOException, ClassNotFoundException, SQLException {
    while (socket.isConnected()) {
      Message message = (Message) in.readObject();
      if (message.getMessageType() == MessageType.REG) {
        registration(message);
      } else if (message.getMessageType() == MessageType.AUTH) {
        LOGGER.info("Authorization: " + message.getLogin() + " " + message.getPassword());
        String nameByLoginPass = authService.getNameByLoginPass(message.getLogin(),
            message.getPassword());
        if (nameByLoginPass != null) {
          if (!server.isOnline(message.getLogin())) {
            login = message.getLogin();
            name = nameByLoginPass;
            completeAuth();
            notifyAboutLogin();
            server.addClient(this);
            return;
          } else {
            sendAuthMessage("Account is already online");
          }
        } else {
          sendAuthMessage("Invalid username/password");
        }
      }
    }
  }

  private void registration(Message regMessage) {
    try {
      authService.insertUser(regMessage.getLogin(), regMessage.getPassword(), regMessage.getName());
      Message message = new Message();
      message.setMessageType(MessageType.REG);
      message.setLogin(regMessage.getLogin());
      send(message);
    } catch (SQLException e) {
      String ex = e.toString();
      Message message = new Message();
      message.setMessageType(MessageType.REG);
      if (ex.contains("UNIQUE constraint failed")) {
        String field = ex.substring(ex.lastIndexOf(".") + 1, ex.length() - 1);
        message.setText("This " + field + " is already in use");
      } else {
        message.setText("Registration failed");
      }
      send(message);
    }
  }

  private void sendAuthMessage(String text) {
    Message message = new Message();
    message.setMessageType(MessageType.AUTH);
    message.setText(text);
    send(message);
    LOGGER.warn(text);
  }

  private void completeAuth() {
    Message message = new Message();
    message.setMessageType(MessageType.CONNECT);
    message.setName(name);
    message.setLogin(login);
    send(message);
  }

  private void notifyAboutLogin() throws SQLException{
    Message message = new Message();
    message.setMessageType(MessageType.SERVER);
    message.setName(name);
    message.setText(" entered the chat");
    historyLogService.insertMessage(message);
    server.broadcastMessage(message);
    LOGGER.info(name + " entered the chat");
  }

  private void readMessage() throws IOException, ClassNotFoundException, SQLException {
    while (socket.isConnected()) {
      Message message = (Message) in.readObject();
      switch (message.getMessageType()) {
        case USER -> broadcastMessage(message);
        case LIST -> server.changeStatus(login, message.getText());
        case DISCONNECT -> closeConnection();
        case SET -> updateSettings(message);
      }
    }
  }

  private void updateSettings(Message setMessage) {
    try {
      if (setMessage.getName() != null) {
        if (setMessage.getPassword() != null) {
          authService.updateNameAndPassword(login, setMessage);
          LOGGER.info(login + " changed name to " + setMessage.getName() + " and password to "
              + setMessage.getPassword());
          sendNewName(setMessage.getName());
        } else {
          authService.updateName(login, setMessage);
          LOGGER.info(login + " changed name to " + setMessage.getName());
          sendNewName(setMessage.getName());
        }
      } else {
        authService.updatePassword(login, setMessage);
        LOGGER.info(login + " changed password to " + setMessage.getPassword());
        sendSetMessage(login);
      }
      server.updateClient(this);
    } catch (SQLException e) {
      String ex = e.toString();
      Message message = new Message();
      message.setMessageType(MessageType.SET);
      if (ex.contains("UNIQUE constraint failed")) {
        String field = ex.substring(ex.lastIndexOf(".") + 1, ex.length() - 1);
        message.setText("This " + field + " is already in use");
      } else {
        message.setText("Saving changes failed");
      }
      send(message);
    }
  }

  private void sendSetMessage(String login) {
    Message message = new Message();
    message.setMessageType(MessageType.SET);
    send(message);
  }


  private void broadcastMessage(Message message) throws SQLException{
    LOGGER.info(name + ": " + message.getText());
    message.setName(name);
    message.setLogin(login);
    historyLogService.insertMessage(message);
    server.broadcastMessage(message);
  }

  public void send(Message message) {
    try {
      out.writeObject(message);
      out.reset();
    } catch (IOException e) {
      LOGGER.error(e);
      LOGGER.debug(e.toString(), e);
    }
  }

  private void closeConnection() {
    LOGGER.info(login + "closed connection");
    if (login != null) {
      server.removeClient(this);
    }
    try {
      notifyAboutExit();
      in.close();
      out.close();
      socket.close();
    } catch (IOException | SQLException e) {
      LOGGER.error(e);
      LOGGER.debug(e.toString(), e);
    }
  }

  private void notifyAboutExit() throws SQLException{
    Message message = new Message();
    message.setMessageType(MessageType.SERVER);
    message.setName(name);
    message.setText(" left the chat");
    historyLogService.insertMessage(message);
    server.broadcastMessage(message);
    LOGGER.info(name + " left the chat");
  }

  private void sendNewName(String newName) throws SQLException{
    String oldName = name;
    name = newName;

    Message message = new Message();
    message.setMessageType(MessageType.SET);
    message.setName(newName);
    send(message);

    message = new Message();
    message.setMessageType(MessageType.SERVER);
    message.setName(oldName);
    message.setText(" changed nickname to " + newName);
    historyLogService.insertMessage(message);
    server.broadcastMessage(message);

  }
}
