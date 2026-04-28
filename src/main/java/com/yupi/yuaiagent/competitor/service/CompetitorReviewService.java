package com.yupi.yuaiagent.competitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuaiagent.competitor.entity.CompetitorReview;

/**
 * 竞品评论数据服务
 */
public interface CompetitorReviewService extends IService<CompetitorReview> {

    Page<CompetitorReview> pageByAppId(String appId, int page, int size);
}
