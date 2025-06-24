#!/bin/bash

# 小智客户端测试项目运行脚本

echo "🚀 启动 xiaozhiclient-test 服务..."

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ 错误: Docker 未安装，请先安装 Docker"
    exit 1
fi

# 检查 docker-compose 是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "❌ 错误: docker-compose 未安装，请先安装 docker-compose"
    exit 1
fi

# 创建日志目录
mkdir -p logs

# 停止并删除现有容器
echo "🔄 清理现有容器..."
docker-compose down

# 构建并启动服务
echo "📦 构建并启动服务..."
docker-compose up --build -d

if [ $? -eq 0 ]; then
    echo "✅ 服务启动成功！"
    echo ""
    echo "🌐 访问地址："
    echo "   http://localhost:8090"
    echo ""
    echo "📋 管理命令："
    echo "   查看日志: docker-compose logs -f"
    echo "   停止服务: docker-compose down"
    echo "   重启服务: docker-compose restart"
    echo ""
    echo "🔍 健康检查："
    echo "   curl http://localhost:8080/health"
else
    echo "❌ 服务启动失败！"
    exit 1
fi 