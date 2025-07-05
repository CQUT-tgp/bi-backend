package com.t.bi.bizmq;

import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMesage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
