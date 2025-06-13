package com.t.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.t.bi.mapper.ChartMapper;
import com.t.bi.model.entity.Chart;

import com.t.bi.service.ChartService;
import org.springframework.stereotype.Service;

/**
* @author T
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2025-06-13 18:00:22
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

}




