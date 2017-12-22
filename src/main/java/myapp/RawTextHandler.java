package myapp;

import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.JSONWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by za-chenshaoang on 2017/12/22.
 */
public class RawTextHandler {
    public static void main(String args[]) throws IOException {
        JSONReader jsonReader = new JSONReader(new FileReader("d://usersays2.json"));
        JSONWriter jsonWriter  = new JSONWriter(new FileWriter("d://usersays_handled.json"));
        jsonWriter.config(SerializerFeature.PrettyFormat,true);
        jsonReader.startArray();
        jsonWriter.startArray();
        Map<String,List<TextUnit>> intentTexts = new HashMap<>();

        while (jsonReader.hasNext()){
            final Text text = jsonReader.readObject(Text.class);
            String intent = text.getIntent();
            final String content = text.getText();
            List<Entity> entities = text.getEntities();
            text.setIntent(intent.replaceAll(" ",""));
            text.setText(content.replaceAll(" ",""));
            if(entities!=null){
                for(Entity entity : entities){
                    entity.setEntity(entity.getEntity().replaceAll(" ",""));
                    entity.setValue(entity.getValue().replaceAll(" ",""));
                }
            }
            jsonWriter.writeObject(text);
        }
        jsonReader.endArray();
        jsonReader.close();
        jsonWriter.endArray();
        jsonWriter.close();
    }
}
