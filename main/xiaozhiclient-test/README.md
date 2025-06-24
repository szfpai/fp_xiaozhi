# 小智客户端测试项目

这是一个用于测试小智语音服务的 Web 客户端项目，使用 nginx 部署。

## 项目结构

```
xiaozhiclient-test/
├── src/
│   └── test/
│       ├── test_page.html          # 完整版测试页面（默认首页）
│       ├── libopus.js              # Opus 音频库
│       ├── abbreviated_version/    # 简化版测试页面
│       │   ├── test.html
│       │   └── app.js
│       └── opus_test/              # Opus 测试页面
├── Dockerfile                      # Docker 构建文件
├── nginx.conf                      # Nginx 配置文件
├── docker-compose.yml              # Docker Compose 配置
├── build.sh                        # 构建脚本
├── run.sh                          # 运行脚本
└── README.md                       # 说明文档
```

## 快速开始

### 使用 Docker Compose（推荐）

1. **启动服务**
   ```bash
   chmod +x run.sh
   ./run.sh
   ```

2. **访问测试页面**
   - 打开浏览器访问：http://localhost:8080
   - 默认显示完整版测试页面
   - 其他测试页面：
     - 简化版测试页面：http://localhost:8080/abbreviated_version/test.html
     - Opus 测试页面：http://localhost:8080/opus_test/test.html

### 手动构建和运行

1. **构建镜像**
   ```bash
   chmod +x build.sh
   ./build.sh
   ```

2. **运行容器**
   ```bash
   docker run -d -p 8080:80 --name xiaozhiclient-test xiaozhiclient-test:latest
   ```

3. **访问服务**
   - 浏览器访问：http://localhost:8080（直接显示完整版测试页面）

## 管理命令

### Docker Compose 命令

```bash
# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down

# 重启服务
docker-compose restart

# 重新构建并启动
docker-compose up --build -d
```

### Docker 命令

```bash
# 查看容器状态
docker ps

# 查看容器日志
docker logs xiaozhiclient-test

# 进入容器
docker exec -it xiaozhiclient-test sh

# 停止容器
docker stop xiaozhiclient-test

# 删除容器
docker rm xiaozhiclient-test

# 删除镜像
docker rmi xiaozhiclient-test:latest
```

## 配置说明

### WebSocket 服务器地址

在测试页面中，默认的 WebSocket 服务器地址是：
- `ws://127.0.0.1:8000/xiaozhi/v1/`

如果您的服务器运行在不同的地址，请在测试页面中修改 WebSocket 地址。

### 端口配置

- 容器内部端口：80
- 主机映射端口：8080

如需修改端口，请编辑 `docker-compose.yml` 文件中的端口映射。

## 功能特性

- ✅ 支持音频录制和播放
- ✅ 支持 WebSocket 连接测试
- ✅ 支持文本消息发送
- ✅ 支持语音识别测试
- ✅ 支持语音合成测试
- ✅ 响应式设计，支持移动端
- ✅ 实时音频可视化
- ✅ 调试信息显示
- ✅ 设备信息配置
- ✅ NFC 卡片测试
- ✅ 多种测试模式

## 故障排除

### 常见问题

1. **端口被占用**
   ```bash
   # 查看端口占用
   lsof -i :8080
   
   # 修改端口映射
   # 编辑 docker-compose.yml，将 8080:80 改为 8081:80
   ```

2. **WebSocket 连接失败**
   - 确保小智服务器正在运行
   - 检查 WebSocket 地址是否正确
   - 检查防火墙设置

3. **音频功能不工作**
   - 确保浏览器支持 Web Audio API
   - 检查麦克风权限设置
   - 尝试使用 HTTPS（某些浏览器要求）

### 日志查看

```bash
# 查看 nginx 访问日志
docker exec xiaozhiclient-test tail -f /var/log/nginx/access.log

# 查看 nginx 错误日志
docker exec xiaozhiclient-test tail -f /var/log/nginx/error.log
```

## 开发说明

### 本地开发

1. 直接在浏览器中打开 HTML 文件
2. 或使用简单的 HTTP 服务器：
   ```bash
   cd src/test
   python -m http.server 8080
   ```

### 自定义配置

如需修改 nginx 配置，请编辑 `nginx.conf` 文件，然后重新构建镜像。

## 许可证

本项目遵循项目的整体许可证。 