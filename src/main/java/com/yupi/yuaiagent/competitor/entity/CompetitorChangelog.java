package com.yupi.yuaiagent.competitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 应用更新日志/版本变化
 */
@Data
@TableName("t_competitor_changelog")
public class CompetitorChangelog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String appId;

    private String version;

    private LocalDate releaseDate;

    private String changelogContent;

    private String diffContent;

    private String extractedChanges;

    private LocalDateTime createdAt;
}
