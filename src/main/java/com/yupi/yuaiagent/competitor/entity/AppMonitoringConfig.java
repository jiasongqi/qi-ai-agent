package com.yupi.yuaiagent.competitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 应用监控配置表
 */
@Data
@TableName("t_app_monitoring_config")
public class AppMonitoringConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String appId;

    private String appName;

    private String category;

    private String subCategory;

    private BigDecimal riskWeight;

    private Boolean isMonitorActive;

    /** 官网 URL */
    private String officialUrl;

    /** 条款/隐私政策 URL */
    private String policyUrl;

    private LocalDateTime lastUpdated;
}
