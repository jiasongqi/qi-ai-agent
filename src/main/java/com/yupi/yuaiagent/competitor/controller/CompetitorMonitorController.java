package com.yupi.yuaiagent.competitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yuaiagent.competitor.entity.*;
import com.yupi.yuaiagent.competitor.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 竞品监控 API 接口
 */
@Slf4j
@RestController
@RequestMapping("/competitor")
@Tag(name = "竞品监控", description = "印尼市场竞品分析智能体相关接口")
public class CompetitorMonitorController {

    @Autowired
    private AppMonitoringConfigService configService;

    @Autowired
    private CompetitorMonitorService monitorService;

    @Autowired
    private CompetitorReviewService reviewService;

    @Autowired
    private CompetitorAnalysisReportService reportService;

    @Autowired
    private AlertService alertService;

    // ==================== App 配置管理 ====================

    @GetMapping("/apps")
    @Operation(summary = "查询监控App列表", description = "支持按category筛选")
    public List<AppMonitoringConfig> listApps(
            @RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return configService.listByCategory(category);
        }
        return configService.list();
    }

    @PostMapping("/apps")
    @Operation(summary = "新增监控App")
    public boolean addApp(@RequestBody AppMonitoringConfig config) {
        return configService.save(config);
    }

    @PutMapping("/apps/{appId}")
    @Operation(summary = "更新App配置")
    public boolean updateApp(@PathVariable String appId, @RequestBody AppMonitoringConfig config) {
        config.setAppId(appId);
        return configService.updateById(config);
    }

    // ==================== 评论数据查询 ====================

    @GetMapping("/reviews")
    @Operation(summary = "分页查询评论数据")
    public Page<CompetitorReview> listReviews(
            @RequestParam(required = false) String appId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return reviewService.pageByAppId(appId, page, size);
    }

    // ==================== 分析报告查询 ====================

    @GetMapping("/reports")
    @Operation(summary = "查询分析报告列表")
    public Page<CompetitorAnalysisReport> listReports(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return reportService.pageReports(page, size);
    }

    @GetMapping("/reports/{id}")
    @Operation(summary = "查看报告详情")
    public CompetitorAnalysisReport getReport(@PathVariable Long id) {
        return reportService.getById(id);
    }

    // ==================== 手动触发分析 ====================

    @PostMapping("/analyze")
    @Operation(summary = "手动触发分析任务", description = "采集指定App评论后执行LLM归因分析，返回分析报告")
    public String triggerAnalysis(@RequestParam String appIds) {
        List<String> idList = Arrays.asList(appIds.split(","));
        return monitorService.triggerManualAnalysis(idList);
    }

    // ==================== 预警管理 ====================

    @GetMapping("/alerts")
    @Operation(summary = "查询预警列表")
    public Page<CompetitorAlert> listAlerts(
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return alertService.pageAlerts(level, page, size);
    }

    @PostMapping("/alerts/{id}/resolve")
    @Operation(summary = "标记预警已处理")
    public boolean resolveAlert(@PathVariable Long id) {
        return alertService.resolveAlert(id);
    }
}
