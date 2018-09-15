package namesayer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

public class Creation {

    private String _name;

    public Creation(){
    }

    public Creation(String name) {
        _name = name;
    }

    public String getCreationName() {
        return this._name;
    }

    public boolean checkValidCreationName(String newName) {

        //Check if the new creation name is empty or null
        if (newName.equals("")|| newName.equals(null)){
            return false;
        }

        //Check if the new creation name only consists of white spaces
        int count = 0;
        for (char ch: newName.toCharArray()){
            if (Character.isWhitespace(ch)){
                count++;
            }
        }
        if (count == newName.length()){
            return false;
        }

        //Remove .mp4 suffix before checking for valid characters
        if(newName.contains(".mp4")){
            String result = newName.replaceAll("\\.mp4", "");
            _name=result;
        }

        //Check for leading/trailing whitespaces
        if (newName.startsWith(" ") || newName.endsWith(" ")) {
            return false;
        }

        //Check for valid characters
        if (newName.matches("^[a-zA-Z0-9 _-]+$")) {
            return true;
        }

        return false;
    }

    public boolean checkNameExists(ObservableList<Creation> creationsList, String creationName){
        for (Creation c: creationsList){
            if (c.getCreationName().equals(creationName)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(){
        return _name;
    }
}

