package namesayer;

import javafx.scene.control.ListCell;

public class CreationCell extends ListCell<Creation> {

    @Override
    public void updateItem(Creation item, boolean empty){
        super.updateItem(item, empty);

        int index = this.getIndex();
        String name = null;

        if (item == null || empty){

        } else {
            name = item.getCreationName();
        }

        this.setText(name);
        setGraphic(null);
    }
}
