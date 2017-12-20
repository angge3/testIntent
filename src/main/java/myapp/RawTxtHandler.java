package myapp;

import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.JSONWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by za-chenshaoang on 2017/12/20.
 */
public class RawTxtHandler {
    public static void main(String args[]) throws IOException {
        JSONReader jsonReader = new JSONReader(new FileReader("d://usersays.json"));
        jsonReader.startArray();
        Map<String,List<String>> intentTexts = new HashMap<>();
        Map<String,List<String>> trainTexts = new HashMap<>();
        Map<String,List<String>> testTexts = new HashMap<>();

        int total = 0;
        int train = 0;
        int test = 0;
        while (jsonReader.hasNext()){
            Text text = jsonReader.readObject(Text.class);
            String intent = text.getIntent();
            final String content = text.getText();
            if(intentTexts.containsKey(intent)){
                intentTexts.get(intent).add(content);
            }else{
                intentTexts.put(intent,new ArrayList<String>(){{
                    add(content);
                }});
            }

        }
        jsonReader.endArray();
        jsonReader.close();
        Set<String> removedKey = new HashSet<>();
        List<String> helloStrs = intentTexts.get("Hello");
        List<String> cuted = helloStrs.subList(0, (int) (helloStrs.size()*0.2));
        intentTexts.put("Hello",cuted);

        for(Map.Entry<String,List<String>> entry : intentTexts.entrySet()){
            String intent = entry.getKey();
            List<String> texts = entry.getValue();

            if(texts.size()<10){
                removedKey.add(intent);
                continue;
            }

            System.out.println(intent+" : "+texts.size());

            int temp = (int)(texts.size()*0.75);

            if(temp > 2000){
                System.out.println(intent +" too much");
                temp = 2000;
            }
            trainTexts.put(intent,texts.subList(0,temp));
            testTexts.put(intent,texts.subList(temp,texts.size()));
        }
        System.out.println("removed keys"+removedKey);
        for(String key : removedKey){
            intentTexts.remove(key);
        }

        for(Map.Entry<String,List<String>> entry : intentTexts.entrySet()){
            total += entry.getValue().size();
        }


        JSONWriter writer = new JSONWriter(new FileWriter("d:/trainTexts.json"));
        writer.config(SerializerFeature.PrettyFormat,true);
        writer.startArray();
        for(Map.Entry<String,List<String>> entry : trainTexts.entrySet()){

            for(String s : entry.getValue()) {
                writer.writeObject(new Text(s,entry.getKey()));
                train++;
            }

        }
        writer.endArray();
        writer.close();

        JSONWriter writer2 = new JSONWriter(new FileWriter("d:/testTexts.json"));
        writer2.config(SerializerFeature.PrettyFormat,true);
        writer2.startArray();
        for(Map.Entry<String,List<String>> entry : testTexts.entrySet()){

            for(String s : entry.getValue()) {
                writer2.writeObject(new Text(s,entry.getKey()));
                test++;
            }

        }
        writer2.endArray();
        writer2.close();

        System.out.println("total : "+total);
        System.out.println("train : "+train);
        System.out.println("test : "+test);
        System.out.println("intent number : "+intentTexts.size());
        System.out.println("intents : "+intentTexts.keySet());
    }
}
