# 使用官方 nginx 镜像作为基础镜像
FROM nginx:alpine

# 设置工作目录
WORKDIR /usr/share/nginx/html

# 复制项目文件到容器中
COPY src/test/ ./test/
COPY src/test/abbreviated_version/ ./abbreviated_version/

# 复制 nginx 配置文件
COPY nginx.conf /etc/nginx/nginx.conf

# 将 test_page.html 设置为默认首页，并复制 libopus.js 到根目录
RUN cp test/test_page.html index.html && \
    cp test/libopus.js ./libopus.js

# 暴露端口
EXPOSE 8090

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"] 