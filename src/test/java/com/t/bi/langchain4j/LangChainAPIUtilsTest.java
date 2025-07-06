package com.t.bi.langchain4j;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class LangChainAPIUtilsTest {
    @Autowired
    private LangChainAPIUtils langChainAPIUtils;
    @Test
    void sendMessage() {
        String message = "你好";
        String result = langChainAPIUtils.sendMessage(message);
        System.out.println(result);
    }

}