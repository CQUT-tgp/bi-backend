package com.t.bi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class SingleCusmer {
    private final static String queueName = "single";
    public static void main(String[] args) throws Exception {
        // 创建连接工厂并设置RabbitMQ主机地址
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");


        // 建立连接和通道
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        // 设置交换机 fanout 交换机都转发  direct 匹配对应的队列转发
        channel.exchangeDeclare("direct_exchange", "direct");
        // 声明队列，确保队列存在
        channel.queueDeclare(queueName, false, false, false, null);

        // 定义消息回调处理逻辑
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String receivedMessage = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + receivedMessage + "'");
        };

        // 订阅消息，自动确认模式
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

}
