package myapp;
import  java.util.*;
/**
 * Created by za-chenshaoang on 2017/12/21.
 */
public class TextUnit {
    private String text;
    private List<Entity> entities;


    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TextUnit(String text, List<Entity> entities) {
        this.text = text;
        this.entities = entities;
    }
}
