package message;

import java.io.Serializable;

public class Message implements Serializable {

  public enum MessageType {
    AUTH, CONNECT, DISCONNECT, USER, SERVER
  }

  private MessageType messageType;
  private String login;
  private String password;
  private String name;
  private String text;

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
    return "Message{" +
        "messageType=" + messageType +
        ", login='" + login + '\'' +
        ", password='" + password + '\'' +
        ", name='" + name + '\'' +
        ", text='" + text + '\'' +
        '}';
  }
}
