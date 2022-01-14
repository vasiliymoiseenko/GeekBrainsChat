package message;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {

  public enum MessageType {
    REG, AUTH, CONNECT, DISCONNECT, USER, SERVER
  }

  private Date date;
  private MessageType messageType;
  private String login;
  private String password;
  private String name;
  private String text;

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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(date + "\n");
    sb.append("\ttype = " + messageType + "\n");
    sb.append((login != null) ? "\tlogin = " + login + "\n" : "");
    sb.append((password != null) ? "\tpassword = " + password + "\n" : "");
    sb.append((name != null) ? "\tname = " + name + "\n" : "");
    sb.append((text != null) ? "\ttext = " + text + "\n" : "");
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }
}
