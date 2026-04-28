-- ============================================
-- 印尼竞品分析智能体 - 初始化数据
-- ============================================

-- 1. 竞品及借贷类 (Category: Competitor)
INSERT INTO t_app_monitoring_config (app_id, app_name, category, sub_category, risk_weight) VALUES
('1435044790', 'Easycash', 'Competitor', '借贷', 0.00),
('1462715669', 'Adakami', 'Competitor', '借贷', 0.00),
('1255413338', 'Kredivo/Kredifazz', 'Competitor', '借贷', 0.00),
('1603402758', 'Rupiah Cepat', 'Competitor', '借贷', 0.00),
('6444848617', 'Kreditpintar', 'Competitor', '借贷', 0.00),
('1485395726', 'Indodana Finance', 'Competitor', '借贷', 0.00),
('6474530590', 'KrediOne', 'Competitor', '借贷', 0.00),
('1473092902', 'Kredito', 'Competitor', '借贷', 0.00),
('6447351335', 'Cairin', 'Competitor', '借贷', 0.00),
('1461448269', 'UangMe', 'Competitor', '借贷', 0.00),
('6477149824', 'Pinjamin - Kredit Dana', 'Competitor', '借贷', 0.00),
('6739596452', 'JULO', 'Competitor', '借贷', 0.00),
('6446885044', 'AmarthaFin', 'Competitor', '借贷', 0.00),
('1564045377', 'Dana kini', 'Competitor', '借贷', 0.00),
('1350403324', 'Pinjam Yuk', 'Competitor', '蝌蚪贷', 0.00),
('1619483828', 'Bantusaku', 'Competitor', '蝌蚪贷', 0.00),
('6476541604', 'PinjamDuit', 'Competitor', '蝌蚪贷', 0.00),
('1610701493', 'Uatas', 'Competitor', '蝌蚪贷', 0.00),
('1661244105', 'FINPLUS', 'Competitor', '蝌蚪贷', 0.00),
('6475002606', 'Samir', 'Competitor', '蝌蚪贷', 0.00),
('6472646842', 'KTA KILAT', 'Competitor', '蝌蚪贷', 0.00),
('1488050503', 'UKU', 'Competitor', '蝌蚪贷', 0.00),
('6444848364', 'Kredinesia', 'Competitor', '蝌蚪贷', 0.00),
('1486755731', 'Solusiku', 'Competitor', '蝌蚪贷', 0.00);

-- 2. 还款能力/白名单类 (Category: Capability)
INSERT INTO t_app_monitoring_config (app_id, app_name, category, sub_category, risk_weight) VALUES
('959841443', 'Shopee Indonesia', 'Capability', '电商', 0.50),
('6455990519', 'ShopeePay', 'Capability', '钱包', 0.40),
('6446321594', 'GoPay', 'Capability', '钱包', 0.40),
('1437123008', 'DANA', 'Capability', '钱包', 0.40),
('944875099', 'Gojek', 'Capability', '出行', 0.30),
('647268330', 'Grab', 'Capability', '出行', 0.30),
('1013717463', 'Alfagift', 'Capability', '电商', 0.20),
('1439730817', 'BRImo BRI', 'Capability', '银行', 0.80),
('1555414743', 'Livin by Mandiri', 'Capability', '银行', 0.80),
('1440241902', 'myBCA', 'Capability', '银行', 0.90),
('6499518320', 'wondr by BNI', 'Capability', '银行', 0.80),
('1525477806', 'SeaBank', 'Capability', '银行', 0.60),
('6444720285', 'Superbank', 'Capability', '银行', 0.60),
('898244857', 'Traveloka', 'Capability', '旅游', 0.40),
('579985456', 'Maxim', 'Capability', '出行', 0.10),
('901804734', 'Access by KAI', 'Capability', '交通', 0.20);

-- 3. 高危/贷超/博彩 (Category: Risk)
INSERT INTO t_app_monitoring_config (app_id, app_name, category, sub_category, risk_weight) VALUES
('PENDING_ID_1', 'Pintar Dana', 'Risk', '贷超', -0.50),
('PENDING_ID_2', 'Dana Rahayu Mobile', 'Risk', '贷超', -0.50),
('PENDING_ID_3', 'Yuk Uang', 'Risk', '贷超', -0.50),
('PENDING_ID_4', 'Tunai Darurat', 'Risk', '贷超', -0.60),
('PENDING_ID_5', 'Uang Pintar', 'Risk', '贷超', -0.50),
('PENDING_ID_6', 'Dompet Tunai', 'Risk', '贷超', -0.50);

-- 4. 工具/越狱/改机 (Category: Tool)
INSERT INTO t_app_monitoring_config (app_id, app_name, category, sub_category, risk_weight) VALUES
('CYDIA_ID', 'Cydia', 'Tool', '越狱', -0.90),
('SILEO_ID', 'Sileo', 'Tool', '越狱', -0.90),
('GAME_ID_1', 'Royal Dream', 'Tool', '博彩游戏', -0.70),
('GAME_ID_2', 'Neo Party', 'Tool', '博彩游戏', -0.70);
