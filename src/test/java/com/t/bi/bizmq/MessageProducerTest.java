package com.t.bi.bizmq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class MessageProducerTest {
    @Resource
    private MessageProducer messageProducer;
    @Test
    void sendMesage() {
        messageProducer.sendMesage("bi", "bi_queue", "hello world");
    }

}