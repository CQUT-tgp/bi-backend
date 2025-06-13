package com.t.bi.utils;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * excel 相关的工具类
 */
@Slf4j
public class ExcelUtils {
    public static String excelTtoCsv(MultipartFile multipartFile){
        // 转换为文件

        List<Map<Integer,String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("excelTtoCsv error",e);
            throw new RuntimeException(e);
        }
        if(CollUtil.isEmpty(list)){
            return "";
        }
        // 转换为csv
        StringBuilder csv = new StringBuilder();
        for (Map<Integer, String> row : list) {
            // 为空的情况
            if(CollUtil.isEmpty(row)){
                continue;
            }
            boolean isStart = true;
            for (Map.Entry<Integer, String> entry : row.entrySet()) {
                if(isStart){
                    isStart = false;
                }else{
                    csv.append(",");
                }
                csv.append(entry.getValue());
            }
            csv.append("\n");
        }
        System.out.println(csv);
        return csv.toString();


    }
}
