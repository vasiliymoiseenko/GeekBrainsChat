package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Listener implements  Runnable{

  private static final String SERVER = "localhost";
  private static final int PORT = 8189;
  private Socket socket;
  private DataInputStream in;
  private DataOutputStream out;
  private ChatController controller;

  public Listener(ChatController controller) {
    this.controller = controller;
  }

  @Override
  public void run() {
    try {
      socket = new Socket(SERVER, PORT);
      in = new DataInputStream(socket.getInputStream());
      out = new DataOutputStream(socket.getOutputStream());
      while (socket.isConnected()) {
        authorization();
        readMessage();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void send(String s) {
    try {
      out.writeUTF(s);
      System.out.println(s);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void authorization() throws IOException {
    while (true) {
      String strFromServer = in.readUTF();
      if (strFromServer.startsWith("/authok")) {
        System.out.println(strFromServer);
        //login = strFromServer.substring("/authok ".length());
        controller.changeStageToChat();
        break;
      }
    }
  }

  private void readMessage() throws IOException{
    while (true) {
      String strFromServer = in.readUTF();
      controller.chat.appendText(strFromServer + "\n");
    }
  }
}
