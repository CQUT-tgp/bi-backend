package com.t.bi.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class BiInitMain {
    // 创建rabbitmq队列和交换机
    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(BiMqConstant.BI_EXCHANGE_NAME, "direct");
            // 创建一个队列，分配队列名称
            channel.queueDeclare(BiMqConstant.BI_QUEUE_NAME, true, false, false, null);
            channel.queueBind(BiMqConstant.BI_QUEUE_NAME, BiMqConstant.BI_EXCHANGE_NAME, BiMqConstant.BI_ROUTING_KEY);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
