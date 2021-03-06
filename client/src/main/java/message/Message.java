package message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Message implements Serializable {

  public enum MessageType {
    REG, AUTH, CONNECT, DISCONNECT, USER, SERVER, LIST, SET;

    public static MessageType fromInt(int i) {
      switch (i) {
        case 0: return REG;
        case 1: return AUTH;
        case 2: return CONNECT;
        case 3: return DISCONNECT;
        case 4: return USER;
        case 5: return SERVER;
        case 6: return LIST;
        case 7: return SET;
        default: return null;
      }
    }
  }

  private Date date;
  private MessageType messageType;
  private String login;
  private String password;
  private String name;
  private String text;
  private List<UserCell> userList;
  private ArrayList<Message> history;

  public Message () {
    this.date = new Date();
  }

  public Date getDate() {
    return date;
  }

  public MessageType getMessageType() {
    return messageType;
  }

  public void setMessageType(MessageType messageType) {
    this.messageType = messageType;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public List<UserCell> getUserList() {
    return userList;
  }

  public void setUserList(HashMap<String, UserCell> userList) {
    this.userList = new ArrayList<>(userList.values());
  }

  public ArrayList<Message> getHistory() {
    return history;
  }

  public void setHistory(ArrayList<Message> history) {
    this.history = history;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(date + "\n");
    sb.append("\ttype = " + messageType + "\n");
    sb.append((login != null) ? "\tlogin = " + login + "\n" : "");
    sb.append((password != null) ? "\tpassword = " + password + "\n" : "");
    sb.append((name != null) ? "\tname = " + name + "\n" : "");
    sb.append((text != null) ? "\ttext = " + text + "\n" : "");
    sb.append((userList != null) ? "\tuserList = " + userList + "\n" : "");
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }
}
