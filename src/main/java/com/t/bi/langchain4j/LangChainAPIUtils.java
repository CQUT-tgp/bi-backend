package com.t.bi.langchain4j;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
//@Slf4j
public class LangChainAPIUtils {
    @Value("${tongyi.api.key}")
    public String AUTHORIZATION;
    @Value("${tongyi.api.url}")
    private String URL;
    @Value("${spark.api.app_id}")
    private String APP_ID;
    public String sendMessage(String message){
        // 使用langchain调用
        /**
         *    <dependency>
         *             <groupId>dev.langchain4j</groupId>
         *             <artifactId>langchain4j-core</artifactId>
         *             <version>1.1.0</version>
         *         </dependency>
         */

        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(AUTHORIZATION)
                .baseUrl(URL)
                .modelName("qwen-plus")
//                .logRequests(true)
//                .logResponses(true)
                .build();
        // 添加系统消息
        ArrayList<Object> list = new ArrayList<>();


        list.add("system:你是一只猫，请用 Cat 的语言回答我的问题");
        list.add("user:你叫什么名字");

        ChatRequest build = new ChatRequest.Builder()
                .messages()
                .build();
        model.doChat(build);
        // 调用chat方法和大模型交互
        String chat = model.chat(message);

        return chat;
    }
    public OpenAiChatModel getModel(){
        OpenAiChatModel model = OpenAiChatModel.builder()
                .apiKey(AUTHORIZATION)
                .baseUrl(URL)
                .modelName("qwen-plus")
//                .logRequests(true)
//                .logResponses(true)
                .build();
        return model;
    }
}
