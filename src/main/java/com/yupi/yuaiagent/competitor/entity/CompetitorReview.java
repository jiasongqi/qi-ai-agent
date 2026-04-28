package com.yupi.yuaiagent.competitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * App Store 评论原始数据
 */
@Data
@TableName("t_competitor_review")
public class CompetitorReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String appId;

    private String reviewId;

    private String reviewerName;

    private Integer rating;

    private String reviewTitle;

    private String reviewContent;

    private LocalDate reviewDate;

    private BigDecimal sentimentScore;

    private String keywords;

    private LocalDateTime createdAt;
}
