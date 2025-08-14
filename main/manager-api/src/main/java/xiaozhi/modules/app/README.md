# 应用管理模块

## 功能概述

应用管理模块是一个完整的应用版本管理系统，支持多平台应用的发布、更新、下载和统计等功能。

## 主要功能

### 1. 应用管理
- **应用信息管理**: 支持添加、编辑、删除应用信息
- **多平台支持**: 支持Android、iOS、Windows、macOS、Linux等平台
- **版本管理**: 支持版本号和版本码管理，便于版本控制
- **文件管理**: 支持应用文件上传、存储和下载

### 2. 应用发布
- **文件上传**: 支持APK、IPA、EXE、DMG、DEB、RPM等格式
- **自动分类**: 根据应用类型自动分类存储
- **文件验证**: 验证文件格式和大小

### 3. 应用更新
- **版本检查**: 提供最新版本查询接口
- **强制更新**: 支持强制更新标记
- **更新日志**: 记录详细的更新内容

### 4. 下载管理
- **下载统计**: 记录下载次数和下载日志
- **二维码生成**: 生成下载链接的二维码
- **用户追踪**: 记录用户IP和用户代理信息

## 技术架构

### 后端架构
- **Controller层**: `AppController` - 提供REST API接口
- **Service层**: `AppService` - 业务逻辑处理
- **DAO层**: `AppDao` - 数据访问层
- **Entity层**: `AppEntity` - 数据实体类

### 前端架构
- **页面组件**: `AppManagement.vue` - 应用管理主页面
- **API接口**: `app.js` - 前端API调用封装
- **路由配置**: 集成到主路由系统

## 数据库管理

### Changelog方式
本项目使用Liquibase进行数据库版本管理，采用changelog方式：

#### 1. 主配置文件
- 位置: `src/main/resources/db/changelog/db.changelog-master.yaml`
- 作用: 管理所有数据库变更的入口文件

#### 2. 应用管理模块变更文件
- 文件: `202507312100.sql`
- 位置: `src/main/resources/db/changelog/202507312100.sql`
- 描述: 应用管理模块的初始数据库结构

#### 3. 变更记录格式
```yaml
- changeSet:
    id: 202507312100
    author: xiaozhi
    changes:
      - sqlFile:
          encoding: utf8
          path: classpath:db/changelog/202507312100.sql
```

### 数据库表结构

#### 应用信息表 (ai_app)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | varchar(64) | 主键ID |
| app_name | varchar(100) | 应用名称 |
| app_type | varchar(50) | 应用类型 |
| version | varchar(50) | 版本号 |
| version_code | int | 版本码 |
| file_size | bigint | 文件大小 |
| file_path | varchar(500) | 文件路径 |
| file_name | varchar(200) | 文件名 |
| download_url | varchar(500) | 下载链接 |
| qr_code_path | varchar(500) | 二维码路径 |
| description | text | 应用描述 |
| changelog | text | 更新日志 |
| is_force_update | tinyint(1) | 是否强制更新 |
| is_active | tinyint(1) | 是否启用 |
| download_count | int | 下载次数 |
| sort | int | 排序 |
| creator | bigint | 创建者 |
| create_date | datetime | 创建时间 |
| updater | bigint | 更新者 |
| update_date | datetime | 更新时间 |

#### 应用下载日志表 (ai_app_download_log)
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | varchar(64) | 主键ID |
| app_id | varchar(64) | 应用ID |
| user_ip | varchar(50) | 用户IP地址 |
| user_agent | varchar(500) | 用户代理 |
| download_time | datetime | 下载时间 |
| download_status | tinyint(1) | 下载状态 |
| error_message | varchar(500) | 错误信息 |
| creator | bigint | 创建者 |
| create_date | datetime | 创建时间 |

### 索引设计
- `idx_ai_app_type`: 应用类型索引
- `idx_ai_app_version_code`: 版本码索引
- `idx_ai_app_is_active`: 启用状态索引
- `idx_ai_app_create_date`: 创建时间索引
- `idx_ai_app_download_log_app_id`: 下载日志应用ID索引
- `idx_ai_app_download_log_download_time`: 下载时间索引
- `idx_ai_app_download_log_user_ip`: 用户IP索引

## API接口说明

### 1. 应用管理接口

#### 分页查询应用列表
```
GET /app/page?page=1&limit=10&appName=xxx&appType=android&isActive=true
```

#### 获取应用详情
```
GET /app/{id}
```

#### 添加应用
```
POST /app
Content-Type: application/json

{
  "appName": "应用名称",
  "appType": "android",
  "version": "1.0.0",
  "versionCode": 1,
  "description": "应用描述",
  "changelog": "更新日志",
  "isForceUpdate": false,
  "isActive": true,
  "sort": 0
}
```

#### 更新应用
```
PUT /app
Content-Type: application/json

{
  "id": "应用ID",
  "appName": "应用名称",
  ...
}
```

#### 删除应用
```
DELETE /app
Content-Type: application/json

["id1", "id2", "id3"]
```

### 2. 文件管理接口

#### 上传应用文件
```
POST /app/upload
Content-Type: multipart/form-data

file: 应用文件
appName: 应用名称
appType: 应用类型
version: 版本号
versionCode: 版本码
description: 应用描述
changelog: 更新日志
isForceUpdate: 是否强制更新
```

#### 下载应用文件
```
GET /app/download/{id}
```

#### 生成下载二维码
```
GET /app/qrcode/{id}
```

### 3. 统计接口

#### 增加下载次数
```
POST /app/download-count/{id}
```

#### 记录下载日志
```
POST /app/download-log
Content-Type: application/json

{
  "appId": "应用ID",
  "userIp": "用户IP",
  "userAgent": "用户代理"
}
```

## 使用说明

### 1. 后端部署
1. **数据库初始化**: 项目启动时会自动执行changelog文件
2. **文件权限**: 确保文件上传目录有写入权限
3. **启动服务**: 启动后端服务，Liquibase会自动管理数据库版本

### 2. 前端集成
1. 将`AppManagement.vue`添加到项目中
2. 配置路由到`/app-management`
3. 在导航菜单中添加应用管理入口

### 3. 权限控制
- 应用管理功能需要超级管理员权限
- 普通用户只能查看和下载应用

### 4. 数据库变更管理
当需要修改数据库结构时：

1. **创建新的changelog文件**:
   - 文件名格式: `YYYYMMDDHHMM.sql`
   - 位置: `src/main/resources/db/changelog/`

2. **在主配置文件中添加引用**:
   ```yaml
   - changeSet:
       id: YYYYMMDDHHMM
       author: 作者名
       changes:
         - sqlFile:
             encoding: utf8
             path: classpath:db/changelog/YYYYMMDDHHMM.sql
   ```

3. **遵循变更原则**:
   - 只允许新建新的changeSet
   - 不允许修改已存在的changeSet
   - 每个变更都要有唯一的ID

## 扩展功能

### 1. 二维码生成
当前版本返回文本链接，可以集成ZXing等库生成真正的二维码图片。

### 2. 文件存储
当前版本使用本地文件系统，可以扩展为云存储（如阿里云OSS、腾讯云COS等）。

### 3. 统计分析
可以添加更详细的下载统计、用户行为分析等功能。

### 4. 应用审核
可以添加应用审核流程，确保应用质量。

## 注意事项

1. **文件安全**: 上传的应用文件需要进行安全扫描
2. **存储空间**: 定期清理旧版本文件，避免存储空间不足
3. **访问控制**: 确保只有授权用户可以访问应用文件
4. **备份策略**: 定期备份应用文件和数据库数据
5. **数据库变更**: 严格按照changelog规范管理数据库版本

## 更新日志

### v1.0.0 (2025-07-31)
- 初始版本发布
- 支持基本的应用管理功能
- 支持文件上传和下载
- 支持版本管理和更新日志
- 采用Liquibase changelog方式管理数据库 