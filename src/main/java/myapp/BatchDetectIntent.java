package myapp;

import com.alibaba.fastjson.JSONReader;
import com.google.cloud.dialogflow.v2beta1.*;
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
public class BatchDetectIntent extends HttpServlet{
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
                Map<String,List<String>> intentTexts = new HashMap<>();
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

                    }else{
                        projectId = item.getString("UTF-8");
                    }
                }
                if(projectId != null) {
                    double total = 0;
                    double right = 0;

                    try (SessionsClient sessionsClient = SessionsClient.create()) {
                        for (Map.Entry<String, List<String>> entry : intentTexts.entrySet()) {
                            String expectedIntent = entry.getKey();
                            List<String> texts = entry.getValue();

                            // Set the session name using the sessionId (UUID) and projectID (my-project-id)
                            SessionName session = SessionName.of(projectId, UUID.randomUUID().toString());
                            System.out.println("Session Path: " + session.toString());

                            // Detect intents for each text input
                            for (String text : texts) {
                                // Set the text (hello) and language code (en-US) for the query
                                TextInput.Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode("zh-CN");

                                // Build the query with the TextInput
                                QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

                                // Performs the detect intent request
                                DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);

                                // Display the query result
                                QueryResult queryResult = response.getQueryResult();

                                System.out.println("====================");
                                System.out.format("Query Text: '%s'\n", queryResult.getQueryText());
                                System.out.format("Detected Intent: %s (confidence: %f)\n",
                                        queryResult.getIntent().getDisplayName(), queryResult.getIntentDetectionConfidence());
                                if(queryResult.getIntent().getDisplayName().equalsIgnoreCase(expectedIntent)){
                                    //识别成功
                                    resp.getWriter().append(queryResult.getQueryText()+" : "+queryResult.getIntent().getDisplayName());
                                    right++;
                                }
                                total++;
                            }
                        }
                        resp.getWriter().append("total : "+total);
                        resp.getWriter().append("right : "+right);
                        resp.getWriter().append("rate : "+right/total);
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
