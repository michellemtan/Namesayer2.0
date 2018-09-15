package namesayer;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class CreationCellFactory implements Callback<ListView<Creation>, ListCell<Creation>> {

    @Override
    public ListCell<Creation> call (ListView<Creation> listview) {
          return new CreationCell();
    }
}
