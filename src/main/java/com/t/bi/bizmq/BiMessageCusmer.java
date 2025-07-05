package com.t.bi.bizmq;

import com.rabbitmq.client.Channel;
import com.t.bi.common.ErrorCode;
import com.t.bi.exception.BusinessException;
import com.t.bi.exception.ThrowUtils;
import com.t.bi.model.entity.Chart;
import com.t.bi.service.ChartService;
import com.t.bi.utils.AiManage;
import com.t.bi.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
@Slf4j
public class BiMessageCusmer {
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private ChartService chartService;
    @Resource
    private AiManage aiManage;
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME},ackMode = "MANUAL")
    public void receive(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        log.info("receive message: {}", message);

            Chart chart = chartService.getById(message);
            if (chart == null){
                log.error("图表不存在"+ message);
                try {
                    channel.basicNack(tag, false,false);
                } catch (IOException e) {
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"确认消息失败");
                }
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
            }
            // 先修改chart 的状态
            Chart chart1 = new Chart();
            chart1.setId(chart.getId());
            chart1.setStatus("running");
            boolean b = chartService.updateById(chart1);
            if (!b){

                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            // 异步处理
            // 获取用户输入
        StringBuilder userInput = new StringBuilder();
            String goal = chart.getGoal();
        // 数据分析师预设
        final String prompt = "你是一个数据分析专家，请根据提供的数据，生成" + goal + "，请使用markdown格式";
        userInput.append("你是一个数据分析师： ");

        String s = chart.getChartData();
        userInput.append("分析目标：").append(goal).append("\n");
        userInput.append("数据：").append("\n").append(s).append("\n");
            String answer = aiManage.doChat(userInput.toString());
            System.out.println(answer+"-------answer");
            // 根据------ 分割结果
            String[] split = answer.split("------");
            String genChart = "";
            String genResult = "";
            if (split.length < 2){
                genChart = split[0];
                genResult = split[0];
            }
            // 更新数据库
            chart1.setStatus("success");
            chart1.setGenChart(genChart);
            chart1.setGenResult(genResult);
            boolean update = chartService.updateById(chart1);
            if (!update){
                try {
                    channel.basicNack(tag, false,false);
                } catch (IOException e) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR,"确认消息失败");
                }
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"更新图表状态失败");
            }

        try {
            channel.basicAck(tag, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
