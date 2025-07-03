package com.t.bi.utils;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AiManageTest {
    @Resource
    private AiManage aiManage;
    @Test
    void doChat() {
        String message = "整数,字符串,小数,日期\n" +
                "1,aa,1.2,2022/10/10 0:00\n" +
                "\n" +
                "你是一个数据分析师： 分析目标：\n" +
                "数据：\n" +
                "整数,字符串,小数,日期\n" +
                "1,aa,1.2,2022/10/10 0:00";
        String result = aiManage.doChat(message);
    }

}