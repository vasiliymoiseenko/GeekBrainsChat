package message;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class UserPicture extends Group {

  private static final int RADIUS = 27;

  public UserPicture() {
    Circle circle = new Circle();
    circle.setRadius(RADIUS);
    circle.setFill(Color.BLACK);
    getChildren().addAll(circle);
  }
}
