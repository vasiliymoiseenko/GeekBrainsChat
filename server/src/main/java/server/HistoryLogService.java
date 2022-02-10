package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import message.Message;
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

  public void insertMessage(Message message) throws SQLException{
    try (PreparedStatement ps = connection.prepareStatement("INSERT INTO log (type, login, name, text) VALUES (?, ?, ?, ?)")) {
      ps.setInt(1, message.getMessageType().ordinal());
      ps.setString(2, message.getLogin());
      ps.setString(3, message.getName());
      ps.setString(4, message.getText());
      ps.executeUpdate();
      LOGGER.debug("Message added to db");
    }

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
