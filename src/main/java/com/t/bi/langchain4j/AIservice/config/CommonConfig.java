//package com.t.bi.langchain4j.AIservice.config;
//
//import com.t.bi.langchain4j.AIservice.ConsultantService;
//import com.t.bi.langchain4j.LangChainAPIUtils;
//import dev.langchain4j.service.AiServices;
//import dev.langchain4j.service.spring.AiService;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.annotation.Resource;
//
//@Configuration
//public class CommonConfig {
//    @Resource
//    private LangChainAPIUtils langChainAPIUtils;
//    @Bean
//    public ConsultantService consultantService()
//    {
//        ConsultantService build = AiServices.builder(ConsultantService.class)
//                .chatModel(langChainAPIUtils.getModel())
//                .build();
//        return build;
//    }
//}
