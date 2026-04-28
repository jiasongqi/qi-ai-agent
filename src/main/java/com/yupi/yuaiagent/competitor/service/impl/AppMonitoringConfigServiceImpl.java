package com.yupi.yuaiagent.competitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuaiagent.competitor.entity.AppMonitoringConfig;
import com.yupi.yuaiagent.competitor.mapper.AppMonitoringConfigMapper;
import com.yupi.yuaiagent.competitor.service.AppMonitoringConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppMonitoringConfigServiceImpl extends ServiceImpl<AppMonitoringConfigMapper, AppMonitoringConfig>
        implements AppMonitoringConfigService {

    @Override
    public List<AppMonitoringConfig> listActiveMonitors() {
        LambdaQueryWrapper<AppMonitoringConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppMonitoringConfig::getIsMonitorActive, true);
        return list(wrapper);
    }

    @Override
    public List<AppMonitoringConfig> listByCategory(String category) {
        LambdaQueryWrapper<AppMonitoringConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppMonitoringConfig::getCategory, category);
        return list(wrapper);
    }
}
