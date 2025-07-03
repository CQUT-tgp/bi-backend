package com.t.bi.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import com.alibaba.fastjson2.JSON;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
@Component
public class Ultra {
    @Value("${spark.api.key}")
    public String AUTHORIZATION;
    @Value("${spark.api.url}")
    private String URL;
    @Value("${spark.api.app_id}")
    private String APP_ID;
    public  Gson gson = new Gson();

    public  List<RoleContent> historyList = new ArrayList<>();

    public  String chat(String message) {
        String prompt = "你是一个数据分析师。我将按照以下格式给你数据：\n" +
                "分析需求：{数据分析需求或者目标}\n" +
                "原始数据：{csv格式的原始数据}\n" +
                "请根据这些内容，生成以下两部分内容：\n" +
                "使用 ----- 五个短横线来分割输出" +
                "-----\n" +
                "{\n" +
                "  前端echarts V5 的 option 配置对象js代码，合理地将数据进行可视化\n" +
                "}\n" +
                "-----\n" +
                "{明确的数据分析结论、越详细越好}\n ----- \n\n\n";
        RoleContent system = new RoleContent();
        system.role = "system";
        system.content = prompt;
        historyList.add(system);
        System.out.println(AUTHORIZATION + "---------- key");
        String userId = "10284711用户";
        StringBuilder sb = new StringBuilder();
            try {
                String url = "https://spark-api-open.xf-yun.com/v1/chat/completions";
                // 创建最外层的JSON对象
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("user", userId);
                jsonObject.put("model", "4.0Ultra");
                // 创建messages数组
                JSONArray messagesArray = new JSONArray();
                // 创建单个消息的JSON对象
                System.out.print("我：");
                Scanner scanner = new Scanner(message);
                String tempQuestion = scanner.nextLine();
                // System.err.println(tempQuestion);
                // 历史信息获取，如果有则携带
                if (historyList.size() > 0) {
                    for (RoleContent tempRoleContent : historyList) {
                        messagesArray.add(JSON.toJSON(tempRoleContent));
                    }
                }
                // 拼接最新问题
                RoleContent roleContent = new RoleContent();
                roleContent.role = "user";
                roleContent.content = tempQuestion;
                messagesArray.add(JSON.toJSON(roleContent));
                historyList.add(roleContent);

                // 将messages数组添加到最外层的JSON对象中
                jsonObject.put("messages", messagesArray);
                // 设置stream属性为true
                jsonObject.put("stream", false);
                jsonObject.put("max_tokens", 8192);
                jsonObject.put("temperature", 0.1);
                // System.err.println(jsonObject);


                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization","Authorization: "+ AUTHORIZATION);
                con.setDoOutput(true);

                OutputStream os = con.getOutputStream();
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = con.getResponseCode();
                // System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                    // System.out.println(inputLine);
                    JsonParse jsonParse = gson.fromJson(inputLine, JsonParse.class);
                    List<Choices> choicesList = jsonParse.choices;
                    for (Choices tempChoices : choicesList) {

                        sb.append(tempChoices.message.content);
                        RoleContent tempRoleContent = new RoleContent();
                        tempRoleContent.setRole("assistant");
                        tempRoleContent.setContent(tempChoices.message.content);
                        historyList.add(roleContent);
                    }
                }
                in.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        return sb.toString();

    }

    static class JsonParse {
        List<Choices> choices;
    }

    static class Choices {
        Message message;
    }

    static class Message {
        String content;
    }

    static class RoleContent {
        String role;
        String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
