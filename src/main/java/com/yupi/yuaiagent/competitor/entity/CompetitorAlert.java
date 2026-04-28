package com.yupi.yuaiagent.competitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能预警记录
 */
@Data
@TableName("t_competitor_alert")
public class CompetitorAlert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String alertType;

    private String appId;

    private String alertLevel;

    private String alertTitle;

    private String alertContent;

    private String relatedData;

    private Boolean isResolved;

    private LocalDateTime createdAt;
}
