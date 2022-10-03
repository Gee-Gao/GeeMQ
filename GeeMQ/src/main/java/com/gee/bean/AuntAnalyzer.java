package com.gee.bean;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName(value = "aunt_analyzer")
public class AuntAnalyzer {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 预计下次日期(日期格式)
     */
    @TableField(value = "next_date")
    private LocalDate nextDate;

    /**
     * 预计下次日期(字符串格式)
     */
    @TableField(value = "next_day_str")
    private String nextDayStr;

    /**
     * 平均天数
     */
    @TableField(value = "avg_day")
    private String avgDay;

    /**
     * 最小天数
     */
    @TableField(value = "min_day")
    private String minDay;

    /**
     * 最大天数
     */
    @TableField(value = "max_day")
    private String maxDay;

    /**
     * 天数出现次数
     */
    @TableField(value = "day_count")
    private String dayCount;

    /**
     * 出现次数最多的天数
     */
    @TableField(value = "days_max")
    private String daysMax;

    /**
     * 提示信息
     */
    @TableField(value = "message")
    private String message;

    /**
     * 排卵期
     */
    @TableField(value = "ovulation")
    private LocalDate ovulation;


    /**
     * 排卵期字符串
     */
    @TableField(value = "ovulation_str")
    private String ovulationStr;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     *  修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}