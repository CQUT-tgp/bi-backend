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

            System.out.println(sparkApiDemo.sendStreamRequest("分析需求：统计某公司第一季度各地区的销售额占比  \n" +
                    "原始数据：Region,Sales  \n" +
                    "North,20000  \n" +
                    "South,15000  \n" +
                    "East,25000  \n" +
                    "West,18000\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
