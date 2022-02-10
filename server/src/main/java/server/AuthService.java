package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import message.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthService {

  private static final Logger LOGGER = LogManager.getLogger(AuthService.class);
  private static Connection connection;
  private static Statement statement;

  private class Entry {

    private String login;
    private String password;
    private String name;

    public Entry(String login, String password, String name) {
      this.login = login;
      this.password = password;
      this.name = name;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  private HashMap<String, Entry> entries = new HashMap<>();

  public void start() {
    try {
      LOGGER.info("AuthService started");
      connect();
      createTable();
      readUsers();
    } catch (SQLException e) {
      LOGGER.error(e);
      LOGGER.debug(e.toString(), e);
    }
  }

  public void stop() {
    LOGGER.info("AuthService stopped");
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      LOGGER.error(e);
      LOGGER.debug(e.toString(), e);
    }
  }

  public String getNameByLoginPass(String login, String password) {
    Entry entry = entries.get(login);
    if (entry != null
        && entry.login.equals(login)
        && entry.password.equals(password)) {
      return entry.name;
    }
    return null;
  }

  public void insertUser(String login, String password, String name) throws SQLException{
    try (PreparedStatement ps = connection.prepareStatement("INSERT INTO users (login, password, name) VALUES (?, ?, ?)")) {
      ps.setString(1, login);
      ps.setString(2, password);
      ps.setString(3, name);
      ps.executeUpdate();
      entries.put(login, new Entry(login, password, name));
      LOGGER.info("User " + login + " added to users.db");
    }
  }

  public void updateNameAndPassword(String login, Message message) throws SQLException{
    try (PreparedStatement ps = connection.prepareStatement("UPDATE users SET name = ?, password = ? WHERE login = ?;")) {
      ps.setString(1, message.getName());
      ps.setString(2, message.getPassword());
      ps.setString(3, login);
      ps.executeUpdate();

      Entry entry = entries.get(login);
      entry.setName(message.getName());
      entry.setPassword(message.getPassword());
    }
  }

  public void updateName(String login, Message message) throws  SQLException{
    try (PreparedStatement ps = connection.prepareStatement("UPDATE users SET name = ? WHERE login = ?;")) {
      ps.setString(1, message.getName());
      ps.setString(2, login);
      ps.executeUpdate();

      entries.get(login).setName(message.getName());
    }
  }

  public void updatePassword(String login, Message message) throws SQLException{
    try (PreparedStatement ps = connection.prepareStatement("UPDATE users SET password = ? WHERE login = ?;")) {
      ps.setString(1, message.getPassword());
      ps.setString(2, login);
      ps.executeUpdate();

      entries.get(login).setName(message.getPassword());
    }
  }

  private void readUsers() throws SQLException{
    try (ResultSet rs = statement.executeQuery("SELECT * FROM users")) {
      while (rs.next()) {
        entries.put(
            rs.getString("login"),
            new Entry(
                rs.getString("login"),
                rs.getString("password"),
                rs.getString("name")));
      }
    }
  }

  private void createTable() throws SQLException {
    statement.executeUpdate("""
        CREATE TABLE IF NOT EXISTS users (
         id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\s
         login TEXT NOT NULL UNIQUE,\s
         password TEXT NOT NULL,\s
         name TEXT NOT NULL UNIQUE\s
        );""");
  }

  private void connect() throws SQLException {
    connection = DriverManager.getConnection("jdbc:sqlite:server/db/users.db");
    statement = connection.createStatement();
  }


}
