package com.t.bi.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
@Component
public class SparkApiUtils{

    // 替换为你的 API Key
    @Value("${spark.api.key}")
    public  String AUTHORIZATION;
    @Value("${spark.api.url}")
    private  String URL;
    @Value("${spark.api.app_id")
    private  String APP_ID;

    /**
     * 发送流式请求并只提取聊天内容
     *
     * @param userQuestion 用户输入的问题
     * @return 完整的模型回复字符串
     */
    public String sendStreamRequest(String userQuestion) throws IOException {
        System.out.println(URL);

        HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // 设置请求头
        connection.setRequestProperty("Authorization", AUTHORIZATION);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("app_id", APP_ID);

        // 构建请求体
        String jsonBody = "{"
                + "\"model\":\"4.0Ultra\","
                + "\"messages\":["
                + "    {\"role\":\"system\",\"content\":\"\"},"
                + "    {\"role\":\"user\",\"content\":\"" + userQuestion + "\"}"
                + "],"
                + "\"max_tokens\":4096,"
                + "\"top_k\":4,"
                + "\"temperature\":0.5,"
                + "\"stream\":true"
                + "}";

        // 发送请求体
        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        StringBuilder fullResponse = new StringBuilder();

        // 只提取 content 部分并拼接输出
        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            char[] buffer = new char[1024];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                String chunk = new String(buffer, 0, bytesRead);
                for (String line : chunk.split("\n")) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6); // 去掉 "data: "
                        if (!"[DONE]".equals(data.trim())) {
                            try {
                                // 解析 JSON 提取 content
                                String content = extractContentFromJson(data);
                                if (content != null && !content.isEmpty()) {
                                    fullResponse.append(content);
                                }
                            } catch (Exception ignored) {
                                // 忽略非标准 JSON 行
                            }
                        }
                    }
                }
            }
        }

        return fullResponse.toString();
    }

    /**
     * 简单解析 JSON 字符串，提取 content 字段值
     */
    private static String extractContentFromJson(String json) {
        int start = json.indexOf("\"content\":\"");
        if (start == -1) return null;
        start += "\"content\":\"".length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }
}
