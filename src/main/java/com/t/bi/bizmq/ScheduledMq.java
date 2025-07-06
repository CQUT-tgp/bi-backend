package com.t.bi.bizmq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.t.bi.model.entity.Chart;
import com.t.bi.service.ChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class ScheduledMq {
    @Autowired
    private BiMessageProducer biMessageProducer;
    @Autowired
    private ChartService chartService;
    @Scheduled(fixedRate = 60000)
    public void reDoWaitingChartTask(){
        // 先查出所有status = waiting
        List<Chart> chartList = chartService.list(new QueryWrapper<Chart>().eq("status", "waiting"));
        // 如果是十分钟之前的任务，那么就重新放入消息队列
        for (Chart chart : chartList) {
            Date updateTime = chart.getUpdateTime();
            if (updateTime.getTime() < System.currentTimeMillis() - 10 * 60 * 1000L) {
                biMessageProducer.sendMesage(chart.getId().toString());
            }
        }

    }
}
