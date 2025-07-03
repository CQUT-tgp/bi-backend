package com.t.bi.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
@Component
public class SparkApiUtils{
    @Resource
    private Ultra ultra;


    /**
     * 发送流式请求并只提取聊天内容
     *
     * @param userQuestion 用户输入的问题
     * @return 完整的模型回复字符串
     */
    public String sendStreamRequest(String userQuestion) throws IOException {



        String userInput =   " 用户输入："+ userQuestion;
        System.out.println(userInput);
        String chat = ultra.chat(userInput);
        return chat;
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
