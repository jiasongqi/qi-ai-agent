package com.yupi.yuaiagent.competitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuaiagent.competitor.entity.CompetitorAnalysisReport;
import com.yupi.yuaiagent.competitor.mapper.CompetitorAnalysisReportMapper;
import com.yupi.yuaiagent.competitor.service.CompetitorAnalysisReportService;
import org.springframework.stereotype.Service;

@Service
public class CompetitorAnalysisReportServiceImpl extends ServiceImpl<CompetitorAnalysisReportMapper, CompetitorAnalysisReport>
        implements CompetitorAnalysisReportService {

    @Override
    public Page<CompetitorAnalysisReport> pageReports(int page, int size) {
        LambdaQueryWrapper<CompetitorAnalysisReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(CompetitorAnalysisReport::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }
}
