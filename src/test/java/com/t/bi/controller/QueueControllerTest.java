package com.t.bi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class QueueControllerTest {
    @Resource
    private QueueController queueController;

    @Test
    void list() {
        queueController.list();
    }

}