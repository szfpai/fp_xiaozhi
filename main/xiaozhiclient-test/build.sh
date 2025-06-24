#!/bin/bash

# 小智客户端测试项目构建脚本

echo "🐳 开始构建 xiaozhiclient-test Docker 镜像..."

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ 错误: Docker 未安装，请先安装 Docker"
    exit 1
fi

# 构建镜像
echo "📦 构建 Docker 镜像..."
docker build -t xiaozhiclient-test:latest .

if [ $? -eq 0 ]; then
    echo "✅ 镜像构建成功！"
    echo ""
    echo "🚀 运行容器："
    echo "   docker run -d -p 8090:8090 --name xiaozhiclient-test xiaozhiclient-test:latest"
    echo ""
    echo "🌐 访问地址："
    echo "   http://localhost:8090"
    echo ""
    echo "📋 其他命令："
    echo "   查看日志: docker logs xiaozhiclient-test"
    echo "   停止容器: docker stop xiaozhiclient-test"
    echo "   删除容器: docker rm xiaozhiclient-test"
else
    echo "❌ 镜像构建失败！"
    exit 1
fi 