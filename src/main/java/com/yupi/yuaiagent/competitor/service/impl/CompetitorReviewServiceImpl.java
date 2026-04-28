package com.yupi.yuaiagent.competitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuaiagent.competitor.entity.CompetitorReview;
import com.yupi.yuaiagent.competitor.mapper.CompetitorReviewMapper;
import com.yupi.yuaiagent.competitor.service.CompetitorReviewService;
import org.springframework.stereotype.Service;

@Service
public class CompetitorReviewServiceImpl extends ServiceImpl<CompetitorReviewMapper, CompetitorReview>
        implements CompetitorReviewService {

    @Override
    public Page<CompetitorReview> pageByAppId(String appId, int page, int size) {
        LambdaQueryWrapper<CompetitorReview> wrapper = new LambdaQueryWrapper<>();
        if (appId != null && !appId.isEmpty()) {
            wrapper.eq(CompetitorReview::getAppId, appId);
        }
        wrapper.orderByDesc(CompetitorReview::getReviewDate);
        return page(new Page<>(page, size), wrapper);
    }
}
