-- 智能体表增加个性化配置
-- 如果列存在，则先删除
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'ai_agent' 
     AND COLUMN_NAME = 'agent_special_config') > 0,
    'ALTER TABLE `ai_agent` DROP COLUMN `agent_special_config`',
    'SELECT "Column does not exist, skipping drop"'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加新列
ALTER TABLE `ai_agent` 
ADD COLUMN `agent_special_config` varchar(64) NULL COMMENT '个性化配置' AFTER `llm_model_id`;

delete from `sys_dict_data` where `dict_type_id` = 101;
INSERT INTO `sys_dict_data` (`id`, `dict_type_id`, `dict_label`, `dict_value`, `remark`, `sort`, `creator`, `create_date`, `updater`, `update_date`) VALUES 
(101001, 101, '喵伴开发板v1', 'EchoEar', '豹拓科技', 56, 1, NOW(), 1, NOW());
INSERT INTO `sys_dict_data` (`id`, `dict_type_id`, `dict_label`, `dict_value`, `remark`, `sort`, `creator`, `create_date`, `updater`, `update_date`) VALUES 
(101002, 101, '喵伴开发板v2', 'EchoEar2', '豹拓科技', 57, 2, NOW(), 1, NOW());

