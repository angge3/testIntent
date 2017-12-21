package myapp;

/**
 * Created by za-chenshaoang on 2017/12/21.
 */
public class Entity {
    private String value;
    private String entity;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public Entity(String value, String entity) {
        this.value = value;
        this.entity = entity;
    }

    public Entity(){

    }
}
