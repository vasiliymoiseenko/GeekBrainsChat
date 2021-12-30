package server;

import java.util.HashMap;

public class AuthService {

  private class Entry {

    private int id;
    private String login;
    private String password;
    private String name;

    public Entry(int id, String login, String password, String name) {
      this.id = id;
      this.login = login;
      this.password = password;
      this.name = name;
    }
  }

  private HashMap<String, Entry> entries = new HashMap<>();

  public void start() {
    System.out.println("AuthService started");
    entries.put("login1", new Entry(1, "login1", "pass1", "nick1"));
    entries.put("login2", new Entry(2, "login2", "pass2", "nick2"));
    entries.put("login3", new Entry(3, "login3", "pass3", "nick3"));
  }

  public void stop() {
    System.out.println("AuthService stopped");
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

}
