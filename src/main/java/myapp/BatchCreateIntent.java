package myapp;

import com.alibaba.fastjson.JSONReader;
import com.google.cloud.dialogflow.v2beta1.EntityType;
import com.google.cloud.dialogflow.v2beta1.Intent;
import com.google.cloud.dialogflow.v2beta1.IntentsClient;
import com.google.cloud.dialogflow.v2beta1.ProjectAgentName;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by za-chenshaoang on 2017/12/18.
 */
public class BatchCreateIntent extends HttpServlet{
    // 上传配置
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB

    private static final String UPLOAD_DIRECTORY = "upload";
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {

        // 配置上传参数
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // 设置内存临界值 - 超过后将产生临时文件并存储于临时目录中
        factory.setSizeThreshold(MEMORY_THRESHOLD);
        // 设置临时存储目录
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

        ServletFileUpload upload = new ServletFileUpload(factory);

        // 设置最大文件上传值
        upload.setFileSizeMax(MAX_FILE_SIZE);

        // 设置最大请求值 (包含文件和表单数据)
        upload.setSizeMax(MAX_REQUEST_SIZE);

        // 中文处理
        upload.setHeaderEncoding("UTF-8");

        // 构造临时路径来存储上传的文件
        // 这个路径相对当前应用的目录

        String uploadPath =  req.getSession().getServletContext().getRealPath("./") + File.separator + UPLOAD_DIRECTORY;


        // 如果目录不存在则创建
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }

        try {
            // 解析请求的内容提取文件数据
            @SuppressWarnings("unchecked")
            List<FileItem> formItems = upload.parseRequest(req);

            String projectId = null;

            if (formItems != null && formItems.size() > 0) {
                Map<String,List<TextUnit>> intentTexts = new HashMap<>();
                // 迭代表单数据
                for (FileItem item : formItems) {
                    // 处理不在表单中的字段
                    if (!item.isFormField()) {
                        String fileName = new File(item.getName()).getName();
                        String filePath = uploadPath + File.separator + fileName;
                        File storeFile = new File(filePath);
                        // 在控制台输出文件的上传路径
                        System.out.println(filePath);
                        // 保存文件到硬盘
                        item.write(storeFile);

                        JSONReader jsonReader = new JSONReader(new FileReader(storeFile));
                        jsonReader.startArray();
                        while (jsonReader.hasNext()){
                            final Text text = jsonReader.readObject(Text.class);
                            String intent = text.getIntent();
                            final String content = text.getText();
                            if(intentTexts.containsKey(intent)){
                                intentTexts.get(intent).add(new TextUnit(content,text.getEntities()));
                            }else{
                                intentTexts.put(intent,new ArrayList<TextUnit>(){{
                                    add(new TextUnit(content,text.getEntities()));
                                }});
                            }
                        }
                        jsonReader.endArray();
                        jsonReader.close();

                    }else{
                        projectId = item.getString("UTF-8");
                    }
                }
                if(projectId != null) {

                    //创建实体
                    Map<String,String> entityTypeMap = new HashMap<>();
                    for (Map.Entry<String, List<TextUnit>> entry : intentTexts.entrySet()) {
                        for(TextUnit textUnit : entry.getValue()){
                            if(textUnit.getEntities()!=null) {
                                for (Entity entity : textUnit.getEntities()) {
                                    entityTypeMap.put(entity.getEntity(),null);
                                }
                            }
                        }
                    }

                    for(String s: entityTypeMap.keySet()){
                        try {
                            EntityType entityType = EntityTypeManagement.createEntityType(s, projectId, "KIND_MAP");
                            entityTypeMap.put(s,entityType.getNameAsEntityTypeName().getEntityType());
                            Thread.sleep(400);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    for (Map.Entry<String, List<TextUnit>> entry : intentTexts.entrySet()) {
                        for(TextUnit textUnit : entry.getValue()){
                            if(textUnit.getEntities()!=null) {
                                for (Entity entity : textUnit.getEntities()) {
                                    try {
                                        EntityManagement.createEntity(projectId, entityTypeMap.get(entity.getEntity()), entity.getValue(), new ArrayList<String>());
                                        Thread.sleep(400);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }

                    try (IntentsClient intentsClient = IntentsClient.create()) {
                        for (Map.Entry<String, List<TextUnit>> entry : intentTexts.entrySet()) {
                            String displayName = entry.getKey();
                            List<TextUnit> txtList = entry.getValue();
                            // Set the project agent name using the projectID (my-project-id)
                            ProjectAgentName parent = ProjectAgentName.of(projectId);

                            // Build the trainingPhrases from the trainingPhrasesParts
                            List<Intent.TrainingPhrase> trainingPhrases = new ArrayList<>();
                            for (TextUnit trainingPhrase : txtList) {
                                Intent.TrainingPhrase.Builder builder = Intent.TrainingPhrase.newBuilder();
                                List<Entity> entities = trainingPhrase.getEntities();


                                List<Intent.TrainingPhrase.Part>  partList = new ArrayList<>();
                                if(entities !=null){
                                    for(Entity entity : entities){
                                        String text = trainingPhrase.getText();
                                        String value = entity.getValue();
                                        int start = text.indexOf(value);
                                        int end = start + value.length();
                                        String first = "";
                                        String last = "";
                                        if(start > 0){
                                            first = value.substring(0,start);
                                        }
                                        if(end<value.length()){
                                            last = value.substring(end);
                                        }
                                        trainingPhrase.setText(first+" "+value+" "+last);
                                    }
                                    String [] words = trainingPhrase.getText().split(" ");
                                    for(String word: words){
                                        for(Entity entity : entities){
                                            if(entity.getValue().equals(word)){
                                                partList.add(Intent.TrainingPhrase.Part.newBuilder()
                                                .setUserDefined(true).setEntityType("@"+entity.getEntity())
                                                .setAlias(entity.getEntity())
                                                .setText(word).build());
                                                break;
                                            }
                                        }
                                        partList.add(Intent.TrainingPhrase.Part.newBuilder()
                                        .setText(word).build());
                                    }
                                }
                                trainingPhrases.add(Intent.TrainingPhrase.newBuilder().addAllParts(partList).build());
                            }

                            // Build the message texts for the agent's response
                            Intent.Message message = Intent.Message.newBuilder()
                                    .setText(
                                            Intent.Message.Text.newBuilder()
                                                    .addAllText(new ArrayList<String>()).build()
                                    ).build();

                            // Build the intent
                            Intent intent = Intent.newBuilder()
                                    .setDisplayName(displayName)
                                    .addMessages(message)
                                    .addAllTrainingPhrases(trainingPhrases)
                                    .setMlEnabled(true)
                                    .build();

                            // Performs the create intent request
                            try {
                                Intent response = intentsClient.createIntent(parent, intent);
                                System.out.format("Intent created: %s\n", response);
                                resp.getWriter().append(response.toString());
                            }catch (Exception e){
                                resp.getWriter().append(e.getMessage());
                            }
                        }
                    }

                }else{
                   resp.getWriter().append("projectId为空");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                resp.getWriter().append(ex.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
