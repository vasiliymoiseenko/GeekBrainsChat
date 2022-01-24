package client;

import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import message.UserPicture;

public class CellRenderer implements Callback<ListView<String>, ListCell<String>> {

  @Override
  public ListCell<String> call(ListView<String> stringListView) {
    ListCell<String> cell = new ListCell<String>() {

      @Override
      protected void updateItem(String name, boolean empty) {
        super.updateItem(name, empty);
        setGraphic(null);
        setText(null);
        if (name != null) {
          HBox hBox = new HBox();
          hBox.setSpacing(10);
          Text nameText = new Text(name);
          UserPicture userPicture = new UserPicture();
          hBox.getChildren().addAll(userPicture, nameText);
          hBox.setAlignment(Pos.CENTER_LEFT);
          setGraphic(hBox);
        }
      }
    };
    return cell;
  }
}
