-- ============================================
-- 印尼竞品分析智能体 - 数据库表结构
-- ============================================

-- 1. 应用监控配置表
CREATE TABLE IF NOT EXISTS t_app_monitoring_config (
    id SERIAL PRIMARY KEY,
    app_id VARCHAR(50) NOT NULL UNIQUE,
    app_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    sub_category VARCHAR(50),
    risk_weight DECIMAL(3, 2),
    is_monitor_active BOOLEAN DEFAULT TRUE,
    official_url VARCHAR(500),
    policy_url VARCHAR(500),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE t_app_monitoring_config IS '应用监控配置表';
COMMENT ON COLUMN t_app_monitoring_config.app_id IS 'iOS App ID';
COMMENT ON COLUMN t_app_monitoring_config.category IS '一级分类：Competitor、Capability、Risk、Tool';
COMMENT ON COLUMN t_app_monitoring_config.risk_weight IS '风险权重：-1.0 到 1.0';
COMMENT ON COLUMN t_app_monitoring_config.official_url IS '官网URL，用于抓取产品动态';
COMMENT ON COLUMN t_app_monitoring_config.policy_url IS '条款/隐私政策URL，用于监控条款变化';

-- 2. 竞品评论原始数据表
CREATE TABLE IF NOT EXISTS t_competitor_review (
    id SERIAL PRIMARY KEY,
    app_id VARCHAR(50) NOT NULL,
    review_id VARCHAR(100),
    reviewer_name VARCHAR(100),
    rating INTEGER,
    review_title VARCHAR(500),
    review_content TEXT,
    review_date DATE,
    sentiment_score DECIMAL(3,2),
    keywords VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_review_app FOREIGN KEY (app_id) REFERENCES t_app_monitoring_config(app_id)
);

CREATE INDEX idx_review_app_id ON t_competitor_review(app_id);
CREATE INDEX idx_review_date ON t_competitor_review(review_date);

COMMENT ON TABLE t_competitor_review IS 'App Store 评论原始数据';

-- 3. 应用更新日志表
CREATE TABLE IF NOT EXISTS t_competitor_changelog (
    id SERIAL PRIMARY KEY,
    app_id VARCHAR(50) NOT NULL,
    version VARCHAR(50),
    release_date DATE,
    changelog_content TEXT,
    diff_content TEXT,
    extracted_changes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_changelog_app FOREIGN KEY (app_id) REFERENCES t_app_monitoring_config(app_id)
);

CREATE INDEX idx_changelog_app_id ON t_competitor_changelog(app_id);

COMMENT ON TABLE t_competitor_changelog IS '应用更新日志/版本变化';

-- 4. 官网政策变化表
CREATE TABLE IF NOT EXISTS t_competitor_policy_change (
    id SERIAL PRIMARY KEY,
    app_id VARCHAR(50) NOT NULL,
    policy_type VARCHAR(50),
    policy_url VARCHAR(500),
    content_snapshot TEXT,
    diff_content TEXT,
    extracted_changes TEXT,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_policy_app FOREIGN KEY (app_id) REFERENCES t_app_monitoring_config(app_id)
);

CREATE INDEX idx_policy_app_id ON t_competitor_policy_change(app_id);

COMMENT ON TABLE t_competitor_policy_change IS '官网条款/政策变化快照';

-- 5. 分析报告表
CREATE TABLE IF NOT EXISTS t_competitor_analysis_report (
    id SERIAL PRIMARY KEY,
    report_date DATE NOT NULL,
    report_type VARCHAR(50),
    target_apps VARCHAR(500),
    summary TEXT,
    key_findings TEXT,
    recommendations TEXT,
    risk_alerts TEXT,
    full_report TEXT,
    generated_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_report_date ON t_competitor_analysis_report(report_date);

COMMENT ON TABLE t_competitor_analysis_report IS 'LLM 生成的结构化分析报告';

-- 6. 预警记录表
CREATE TABLE IF NOT EXISTS t_competitor_alert (
    id SERIAL PRIMARY KEY,
    alert_type VARCHAR(50),
    app_id VARCHAR(50),
    alert_level VARCHAR(20),
    alert_title VARCHAR(200),
    alert_content TEXT,
    related_data TEXT,
    is_resolved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_alert_level ON t_competitor_alert(alert_level);
CREATE INDEX idx_alert_created ON t_competitor_alert(created_at);

COMMENT ON TABLE t_competitor_alert IS '智能预警记录';
