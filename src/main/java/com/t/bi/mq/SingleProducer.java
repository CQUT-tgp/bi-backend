package com.t.bi.mq;

import com.alibaba.dashscope.common.Message;
import com.rabbitmq.client.*;

public class SingleProducer {
    private final static String queueName = "single";
    public static void main(String[] args) throws Exception {
        // 创建连接工厂并设置RabbitMQ主机地址
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        // 建立连接和通道
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        // 声明队列，确保队列存在
        channel.queueDeclare(queueName, false, false, false, null);

        // 发送消息到队列
        String message = "Hello RabbitMQ!";
        for (int i = 0; i < 10000; i++) {
            message = "Hello RabbitMQ!" + i;
            channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "'");

        }

        // 输出提示信息
        System.out.println(" miao ");

        // 定义消息回调处理逻辑
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String receivedMessage = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + receivedMessage + "'");
        };

        // 订阅消息，自动确认模式
//        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

}
