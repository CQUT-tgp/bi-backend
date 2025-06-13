package com.t.bi.utils;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

@SpringBootTest
public class TongYiTest {
    @Resource
    private SparkApiUtils sparkApiDemo;
    @Test
    public void test() {
        try {
            System.out.println(sparkApiDemo.AUTHORIZATION);
            System.out.println(sparkApiDemo.sendStreamRequest("你是谁"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
