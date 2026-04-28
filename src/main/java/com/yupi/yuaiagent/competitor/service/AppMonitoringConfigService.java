package com.yupi.yuaiagent.competitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuaiagent.competitor.entity.AppMonitoringConfig;

import java.util.List;

/**
 * 应用监控配置服务
 */
public interface AppMonitoringConfigService extends IService<AppMonitoringConfig> {

    /**
     * 获取所有活跃的监控配置
     */
    List<AppMonitoringConfig> listActiveMonitors();

    /**
     * 按分类获取监控配置
     */
    List<AppMonitoringConfig> listByCategory(String category);
}
