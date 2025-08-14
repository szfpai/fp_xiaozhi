-- 应用管理模块数据库表结构
-- 创建时间: 2025-07-31 21:00
-- 作者: xiaozhi
-- 描述: 新增应用管理模块，支持多平台应用发布、版本管理、下载统计等功能

-- 删除已存在的表（如果存在）
DROP TABLE IF EXISTS `ai_app_download_log`;
DROP TABLE IF EXISTS `ai_app`;

-- 应用信息表
CREATE TABLE `ai_app` (
    `id` VARCHAR(64) NOT NULL COMMENT '主键ID',
    `app_name` VARCHAR(100) NOT NULL COMMENT '应用名称',
    `app_type` VARCHAR(50) NOT NULL COMMENT '应用类型(android/ios/windows/macos/linux)',
    `version` VARCHAR(50) NOT NULL COMMENT '版本号',
    `version_code` INT NOT NULL COMMENT '版本码',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件存储路径',
    `file_name` VARCHAR(200) DEFAULT NULL COMMENT '文件名',
    `download_url` VARCHAR(500) DEFAULT NULL COMMENT '下载链接',
    `qr_code_path` VARCHAR(500) DEFAULT NULL COMMENT '二维码图片路径',
    `description` TEXT COMMENT '应用描述',
    `changelog` TEXT COMMENT '更新日志',
    `is_force_update` TINYINT(1) DEFAULT 0 COMMENT '是否强制更新(0:否,1:是)',
    `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用(0:禁用,1:启用)',
    `download_count` INT DEFAULT 0 COMMENT '下载次数',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `creator` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_date` DATETIME DEFAULT NULL COMMENT '创建时间',
    `updater` BIGINT DEFAULT NULL COMMENT '更新者',
    `update_date` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用信息表';

-- 应用下载日志表
CREATE TABLE `ai_app_download_log` (
    `id` VARCHAR(64) NOT NULL COMMENT '主键ID',
    `app_id` VARCHAR(64) NOT NULL COMMENT '应用ID',
    `user_ip` VARCHAR(50) DEFAULT NULL COMMENT '用户IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `download_time` DATETIME DEFAULT NULL COMMENT '下载时间',
    `download_status` TINYINT(1) DEFAULT 1 COMMENT '下载状态(0:失败,1:成功)',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `creator` BIGINT DEFAULT NULL COMMENT '创建者',
    `create_date` DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用下载日志表';

-- 创建索引
CREATE INDEX `idx_ai_app_type` ON `ai_app` (`app_type`);
CREATE INDEX `idx_ai_app_version_code` ON `ai_app` (`version_code`);
CREATE INDEX `idx_ai_app_is_active` ON `ai_app` (`is_active`);
CREATE INDEX `idx_ai_app_create_date` ON `ai_app` (`create_date`);
CREATE INDEX `idx_ai_app_download_log_app_id` ON `ai_app_download_log` (`app_id`);
CREATE INDEX `idx_ai_app_download_log_download_time` ON `ai_app_download_log` (`download_time`);
CREATE INDEX `idx_ai_app_download_log_user_ip` ON `ai_app_download_log` (`user_ip`);

-- 插入示例数据
INSERT INTO `ai_app` (`id`, `app_name`, `app_type`, `version`, `version_code`, `file_size`, `file_path`, `file_name`, `download_url`, `description`, `changelog`, `is_force_update`, `is_active`, `download_count`, `sort`, `creator`, `create_date`, `updater`, `update_date`) VALUES
('1', '喵伴助手Android版', 'android', '1.0.0', 1, 52428800, 'uploads/apps/android/sample.apk', 'xiaozhi-assistant-v1.0.0.apk', '/app/download/1', '小智智能助手Android版本，提供智能对话、语音识别等功能', '1. 新增智能对话功能\n2. 支持语音识别\n3. 优化用户界面\n4. 修复已知问题', 0, 1, 0, 1, 1, NOW(), 1, NOW()),
('2', '喵伴助手iOS版', 'ios', '1.0.0', 1, 47185920, 'uploads/apps/ios/sample.ipa', 'xiaozhi-assistant-v1.0.0.ipa', '/app/download/2', '小智智能助手iOS版本，提供智能对话、语音识别等功能', '1. 新增智能对话功能\n2. 支持语音识别\n3. 优化用户界面\n4. 修复已知问题', 0, 1, 0, 2, 1, NOW(), 1, NOW()); 

delete from `sys_dict_data` where `dict_type_id` = 101;
INSERT INTO `sys_dict_data` (`id`, `dict_type_id`, `dict_label`, `dict_value`, `remark`, `sort`, `creator`, `create_date`, `updater`, `update_date`) VALUES 
(101001, 101, '喵伴开发板v1', 'EchoEar_v1', '豹拓科技', 56, 1, NOW(), 1, NOW());
INSERT INTO `sys_dict_data` (`id`, `dict_type_id`, `dict_label`, `dict_value`, `remark`, `sort`, `creator`, `create_date`, `updater`, `update_date`) VALUES 
(101002, 101, '喵伴开发板v2', 'EchoEar_v2', '豹拓科技', 57, 2, NOW(), 1, NOW());
