package com.t.bi.utils;


import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service
public class AiManage {
    @Resource
    private SparkApiUtils sparkApiUtils;
    public String doChat(String mesasge){
        try {
            System.out.println(mesasge+"-------message");

            // 修改为异步
            String s = sparkApiUtils.sendStreamRequest(mesasge);
            return s;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
