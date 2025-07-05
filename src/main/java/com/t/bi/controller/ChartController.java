package com.t.bi.controller;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.t.bi.annotation.AuthCheck;
import com.t.bi.bizmq.BiMessageProducer;
import com.t.bi.common.BaseResponse;
import com.t.bi.common.DeleteRequest;
import com.t.bi.common.ErrorCode;
import com.t.bi.common.ResultUtils;
import com.t.bi.constant.CommonConstant;
import com.t.bi.constant.FileConstant;
import com.t.bi.constant.UserConstant;
import com.t.bi.exception.BusinessException;
import com.t.bi.exception.ThrowUtils;
import com.t.bi.manager.RedisLimiterManager;
import com.t.bi.model.dto.chart.*;
import com.t.bi.model.dto.file.UploadFileRequest;
import com.t.bi.model.entity.Chart;
import com.t.bi.model.entity.User;
import com.t.bi.model.enums.FileUploadBizEnum;
import com.t.bi.model.vo.BiResponse;
import com.t.bi.service.ChartService;
import com.t.bi.service.UserService;
import com.t.bi.utils.AiManage;
import com.t.bi.utils.ExcelUtils;
import com.t.bi.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {
    @Resource
    private BiMessageProducer biMessageProducer;

    @Resource
    private ChartService chartService;
    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private UserService userService;
    @Resource
    private AiManage aiManage;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }
//  上传文件，打印里面的文字
@PostMapping("/upload")
public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // 文件是text类型 直接打印文字,流式读取
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
    return ResultUtils.success("success");

}


    /**
     * 文件上传
     *
     * @param multipartFile
//     * @param genChartByAiRequest
//     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(
            @ModelAttribute("genChartByAiRequest") GenChartByAiRequest genChartByAiRequest,
            @RequestPart("file") MultipartFile multipartFile,
            HttpServletRequest request
            ) {
        // 限流判断
        redisLimiterManager.doRateLimit("gen:"+userService.getLoginUser(request).getId());
        System.out.println("------- 请求进行中");
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
//        String name = "1";
//        String goal = "1";
//        String chartType = "1";
        System.out.println("---------运行到这");
        // 获取文件名，然后检查后缀是否为 excel 格式
        String filename = multipartFile.getOriginalFilename();
        ThrowUtils.throwIf(!filename.endsWith(".xlsx"), ErrorCode.PARAMS_ERROR, "文件格式错误");
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        StringBuilder userInput = new StringBuilder();
        // 数据分析师预设
        final String prompt = "你是一个数据分析专家，请根据提供的数据，生成" + goal + "，请使用markdown格式";
        userInput.append("你是一个数据分析师： ");
        // 先要把文件保存到本地，然后异步解析
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "未选择文件");

        String s = ExcelUtils.excelTtoCsv(multipartFile);
        userInput.append("分析目标：").append(goal).append("\n");
        userInput.append("数据：").append("\n").append(s).append("\n");
        // 先将数据保存到数据库，然后调用异步接口处理
        BiResponse biResponse = new BiResponse();
        // 放入数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(s);
        chart.setChartType(chartType);
        chart.setStatus("waiting");
        // 获取userid
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        chartService.save(chart);
        Long id = chart.getId();
        biMessageProducer.sendMesage(String.valueOf(id));
        // 调用通义千问的接口
//        CompletableFuture.runAsync(() -> {
//
//            // 先修改chart 的状态
//            Chart chart1 = new Chart();
//            chart1.setId(chart.getId());
//            chart1.setStatus("running");
//            boolean b = chartService.updateById(chart1);
//            if (!b){
//
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
//            }
//            // 异步处理
//            // 获取用户输入
//
//            String answer = aiManage.doChat(userInput.toString());
//            System.out.println(answer+"-------answer");
//            // 根据------ 分割结果
//            String[] split = answer.split("------");
//            String genChart = "";
//            String genResult = "";
//            if (split.length < 2){
//                genChart = split[0];
//                genResult = split[0];
//            }
//            // 更新数据库
//            chart1.setStatus("success");
//            chart1.setGenChart(genChart);
//            chart1.setGenResult(genResult);
//            boolean update = chartService.updateById(chart1);
//            if (!update){
//                throw new BusinessException(ErrorCode.OPERATION_ERROR,"更新图表状态失败");
//            }
//        }, threadPoolExecutor);



        return ResultUtils.success(biResponse);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


}
