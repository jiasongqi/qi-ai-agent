package com.yupi.yuaiagent.competitor.scheduler;

import com.yupi.yuaiagent.competitor.service.CompetitorMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 竞品监控定时调度器
 * 每天上午9:00自动执行竞品数据采集任务
 */
@Slf4j
@Component
public class CompetitorMonitorScheduler {

    @Autowired
    private CompetitorMonitorService competitorMonitorService;

    /**
     * 每日9:00执行竞品监控任务
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void dailyCompetitorMonitor() {
        log.info("============= 每日竞品监控任务开始 =============");
        long startTime = System.currentTimeMillis();
        try {
            competitorMonitorService.executeDailyMonitor();
            long duration = System.currentTimeMillis() - startTime;
            log.info("============= 每日竞品监控任务完成，耗时 {}ms =============", duration);
        } catch (Exception e) {
            log.error("每日竞品监控任务执行异常: {}", e.getMessage(), e);
        }
    }
}
