package com.yupi.yuaiagent.competitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuaiagent.competitor.entity.CompetitorAlert;
import com.yupi.yuaiagent.competitor.mapper.CompetitorAlertMapper;
import com.yupi.yuaiagent.competitor.service.AlertService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertServiceImpl extends ServiceImpl<CompetitorAlertMapper, CompetitorAlert>
        implements AlertService {

    @Override
    public void createAlert(String alertType, String appId, String level, String title, String content, String relatedData) {
        CompetitorAlert alert = new CompetitorAlert();
        alert.setAlertType(alertType);
        alert.setAppId(appId);
        alert.setAlertLevel(level);
        alert.setAlertTitle(title);
        alert.setAlertContent(content);
        alert.setRelatedData(relatedData);
        alert.setIsResolved(false);
        save(alert);
    }

    @Override
    public List<CompetitorAlert> listUnresolvedAlerts() {
        LambdaQueryWrapper<CompetitorAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CompetitorAlert::getIsResolved, false)
               .orderByDesc(CompetitorAlert::getCreatedAt);
        return list(wrapper);
    }

    @Override
    public Page<CompetitorAlert> pageAlerts(String level, int page, int size) {
        LambdaQueryWrapper<CompetitorAlert> wrapper = new LambdaQueryWrapper<>();
        if (level != null && !level.isEmpty()) {
            wrapper.eq(CompetitorAlert::getAlertLevel, level);
        }
        wrapper.orderByDesc(CompetitorAlert::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public boolean resolveAlert(Long alertId) {
        CompetitorAlert alert = getById(alertId);
        if (alert == null) {
            return false;
        }
        alert.setIsResolved(true);
        return updateById(alert);
    }
}
