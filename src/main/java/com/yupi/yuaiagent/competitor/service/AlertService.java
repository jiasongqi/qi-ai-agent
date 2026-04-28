package com.yupi.yuaiagent.competitor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuaiagent.competitor.entity.CompetitorAlert;

import java.util.List;

/**
 * 预警服务
 */
public interface AlertService extends IService<CompetitorAlert> {

    void createAlert(String alertType, String appId, String level, String title, String content, String relatedData);

    List<CompetitorAlert> listUnresolvedAlerts();

    Page<CompetitorAlert> pageAlerts(String level, int page, int size);

    boolean resolveAlert(Long alertId);
}
