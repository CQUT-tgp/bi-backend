package com.t.bi.controller;

import com.t.bi.config.ThreadPoolExecutorConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Controller
@RequestMapping("/queue")
public class QueueController {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @RequestMapping("/list")
    public String list() {
        CompletableFuture.runAsync(
                () -> {
                    System.out.println("hello world");
                },
                threadPoolExecutor
        );
        System.out.println(threadPoolExecutor.getQueue().size());
        System.out.println(threadPoolExecutor.getCompletedTaskCount());
        System.out.println(threadPoolExecutor.getActiveCount());
        return "queue";
    }
}
