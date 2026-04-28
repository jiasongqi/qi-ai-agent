package com.yupi.yuaiagent.competitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 官网条款/政策变化快照
 */
@Data
@TableName("t_competitor_policy_change")
public class CompetitorPolicyChange {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String appId;

    private String policyType;

    private String policyUrl;

    private String contentSnapshot;

    private String diffContent;

    private String extractedChanges;

    private LocalDateTime detectedAt;
}
