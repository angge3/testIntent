package myapp;

/**
 * Created by za-chenshaoang on 2017/12/19.
 */
import  java.util.List;

public class Text {
    private String text;
    private String intent;
    private List<Entity> entities;


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }


    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entityList) {
        this.entities = entityList;
    }

    public Text(String text, String intent, List<Entity> entities) {
        this.text = text;
        this.intent = intent;
        this.entities = entities;
    }
}
