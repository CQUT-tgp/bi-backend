package com.t.bi.model.dto.chart;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * 文件上传请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class GenChartByAiRequest implements Serializable {

    /**
     * 名称
     */
    private String name;
    /**
     * 分析目标
     */
    private String goal;


    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 图表数据
     */
    private String chartData;


    private static final long serialVersionUID = 1L;
}