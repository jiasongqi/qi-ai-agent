package com.yupi.yuaiagent.competitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * LLM 生成的结构化分析报告
 */
@Data
@TableName("t_competitor_analysis_report")
public class CompetitorAnalysisReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate reportDate;

    private String reportType;

    private String targetApps;

    private String summary;

    private String keyFindings;

    private String recommendations;

    private String riskAlerts;

    private String fullReport;

    private String generatedBy;

    private LocalDateTime createdAt;
}
