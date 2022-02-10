package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import message.Message;
import message.Message.MessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HistoryLogService {

  private static final Logger LOGGER = LogManager.getLogger(AuthService.class);
  private static Connection connection;
  private static Statement statement;

  public void start() {
    try {
      LOGGER.info("HistoryLogService started");
      connect();
      createTable();
    } catch (SQLException e) {
      LOGGER.error(e);
      LOGGER.debug(e.toString(), e);
    }
  }

  public void stop() {
    LOGGER.info("HistoryLogService stopped");
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      LOGGER.error(e);
      LOGGER.debug(e.toString(), e);
    }
  }

  public void insertMessage(Message message) throws SQLException {
    try (PreparedStatement ps = connection.prepareStatement(
        "INSERT INTO log (type, login, name, text) VALUES (?, ?, ?, ?)")) {
      ps.setInt(1, message.getMessageType().ordinal());
      ps.setString(2, message.getLogin());
      ps.setString(3, message.getName());
      ps.setString(4, message.getText());
      ps.executeUpdate();
      LOGGER.debug("Message added to db");
    }
  }

  public ArrayList<Message> readLog(int size) throws SQLException {
    ArrayList<Message> result = new ArrayList<>();
    String sql = "SELECT * FROM (SELECT * FROM log ORDER BY id DESC LIMIT " + size + ") t ORDER BY id";
    //String sql = "SELECT * FROM log LIMIT " + count;
    try (ResultSet rs = statement.executeQuery(sql)) {
      while (rs.next()) {
        Message message = new Message();
        message.setMessageType(MessageType.fromInt(rs.getInt("type")));
        message.setLogin(rs.getString("login"));
        message.setName(rs.getString("name"));
        message.setText(rs.getString("text"));
        result.add(message);
      }
    }
    return result;
  }

  private void connect() throws SQLException {
    connection = DriverManager.getConnection("jdbc:sqlite:server/db/history_log.db");
    statement = connection.createStatement();
  }

  private void createTable() throws SQLException {
    statement.executeUpdate("""
        CREATE TABLE IF NOT EXISTS log (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\s
            type INTEGER NOT NULL,\s
            login TEXT,\s
            name TEXT,\s
            text TEXT\s
        );""");
  }
}
