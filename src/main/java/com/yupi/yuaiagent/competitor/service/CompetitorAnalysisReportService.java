package com.yupi.yuaiagent.competitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuaiagent.competitor.entity.CompetitorAnalysisReport;

public interface CompetitorAnalysisReportService extends IService<CompetitorAnalysisReport> {

    Page<CompetitorAnalysisReport> pageReports(int page, int size);
}
