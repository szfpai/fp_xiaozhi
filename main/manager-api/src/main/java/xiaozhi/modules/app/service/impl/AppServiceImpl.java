package xiaozhi.modules.app.service.impl;

import xiaozhi.common.service.impl.BaseServiceImpl;
import xiaozhi.common.page.PageData;
import xiaozhi.modules.app.dao.AppDao;
import xiaozhi.modules.app.entity.AppEntity;
import xiaozhi.modules.app.service.AppService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Date;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;

@Service
public class AppServiceImpl extends BaseServiceImpl<AppDao, AppEntity> implements AppService {
    
    @Value("${server.port:8002}")
    private int serverPort;
    
    @Value("${server.servlet.context-path:/fpsphere}")
    private String contextPath;

    @Override
    public PageData<AppEntity> page(Map<String, Object> params) {
        try {
            // 获取分页参数，使用安全的转换方式
            Integer page = 1;
            Integer limit = 10;
            
            try {
                if (params.get("page") != null) {
                    page = Integer.parseInt(params.get("page").toString());
                }
                if (params.get("limit") != null) {
                    limit = Integer.parseInt(params.get("limit").toString());
                }
            } catch (NumberFormatException e) {
                System.err.println("分页参数转换失败，使用默认值: " + e.getMessage());
            }
            
            // 构建查询条件
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AppEntity> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            
            // 应用名称模糊查询
            if (params.get("appName") != null && !params.get("appName").toString().trim().isEmpty()) {
                queryWrapper.like("app_name", params.get("appName").toString().trim());
            }
            
            // 应用类型查询
            if (params.get("appType") != null && !params.get("appType").toString().trim().isEmpty()) {
                queryWrapper.eq("app_type", params.get("appType").toString().trim());
            }
            
            // 是否启用查询
            if (params.get("isActive") != null) {
                try {
                    Boolean isActive = Boolean.parseBoolean(params.get("isActive").toString());
                    queryWrapper.eq("is_active", isActive);
                } catch (Exception e) {
                    System.err.println("isActive参数转换失败: " + e.getMessage());
                }
            }
            
            // 排序
            queryWrapper.orderByDesc("create_date");
            
            // 分页查询
            com.baomidou.mybatisplus.core.metadata.IPage<AppEntity> pageResult = 
                baseDao.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, limit), queryWrapper);
            
            return new PageData<>(pageResult.getRecords(), pageResult.getTotal());
        } catch (Exception e) {
            System.err.println("分页查询失败: " + e.getMessage());
            e.printStackTrace();
            // 返回空的分页结果
            return new PageData<>(new ArrayList<>(), 0);
        }
    }

    @Override
    public boolean save(AppEntity entity) {
        // 设置创建时间
        entity.setCreateDate(new Date());
        entity.setUpdateDate(new Date());
        
        // 设置默认值
        if (entity.getDownloadCount() == null) {
            entity.setDownloadCount(0);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }
        if (entity.getIsForceUpdate() == null) {
            entity.setIsForceUpdate(false);
        }
        
        return insert(entity);
    }

    @Override
    public void update(AppEntity entity) {
        entity.setUpdateDate(new Date());
        updateById(entity);
    }

    @Override
    public void delete(String[] ids) {
        for (String id : ids) {
            // 删除应用文件
            AppEntity app = selectById(id);
            if (app != null && app.getFilePath() != null) {
                try {
                    File file = new File(app.getFilePath());
                    if (file.exists()) {
                        file.delete();
                    }
                } catch (Exception e) {
                    // 记录日志但不影响删除操作
                    System.err.println("删除应用文件失败: " + e.getMessage());
                }
            }
            
            // 删除数据库记录
            deleteById(id);
        }
    }

    @Override
    public AppEntity getLatestApp(String appType) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AppEntity> queryWrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("app_type", appType);
        queryWrapper.eq("is_active", true);
        queryWrapper.orderByDesc("version_code");
        queryWrapper.last("LIMIT 1");
        
        return baseDao.selectOne(queryWrapper);
    }

    @Override
    public AppEntity uploadAppFile(MultipartFile file, String appName, String appType, 
                                 String version, Integer versionCode, String description, 
                                 String changelog, Boolean isForceUpdate) {
        try {
            // 创建应用实体
            AppEntity app = new AppEntity();
            app.setAppName(appName);
            app.setAppType(appType);
            app.setVersion(version);
            app.setVersionCode(versionCode);
            app.setDescription(description);
            app.setChangelog(changelog);
            app.setIsForceUpdate(isForceUpdate);
            app.setFileSize(file.getSize());
            app.setFileName(file.getOriginalFilename());
            
            // 生成文件存储路径 - 使用挂载的目录
            String uploadDir = "uploadfile/apps/" + appType + "/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 确保目录存在
            if (!Files.exists(uploadPath)) {
                throw new RuntimeException("无法创建上传目录: " + uploadDir);
            }
            
            // 生成唯一文件名
            String fileExtension = "";
            if (file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")) {
                fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + fileExtension;
            String filePath = uploadDir + fileName;
            
            // 保存文件
            Path targetPath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath);
            
            // 存储相对路径
            app.setFilePath(filePath);
            
            // 保存到数据库
            save(app);
            
            // 设置下载链接（需要在保存后获取ID）
            app.setDownloadUrl("/app/download/" + app.getId());
            // 更新下载链接
            updateById(app);
            
            return app;
        } catch (Exception e) {
            // 记录详细错误信息
            System.err.println("上传应用文件失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("上传应用文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void downloadApp(String appId, HttpServletResponse response) {
        AppEntity app = selectById(appId);
        if (app == null || app.getFilePath() == null) {
            throw new RuntimeException("应用文件不存在");
        }
        
        // 添加调试日志
        System.out.println("下载应用 - ID: " + appId);
        System.out.println("应用名称: " + app.getAppName());
        System.out.println("文件路径: " + app.getFilePath());
        System.out.println("文件名: " + app.getFileName());
        System.out.println("当前工作目录: " + System.getProperty("user.dir"));
        
        try {
            String filePath = app.getFilePath();
            File file = null;
            
            // 相对路径，尝试多个可能的根目录
            String[] possibleRoots = {
                System.getProperty("user.dir"),                    // 当前工作目录
                System.getProperty("user.dir") + "/uploadfile",    // 当前目录下的uploadfile
                System.getProperty("user.dir") + "/../uploadfile", // 上级目录下的uploadfile
                System.getProperty("user.dir") + "/../../uploadfile", // 上上级目录下的uploadfile
                System.getProperty("user.dir") + "/../../../uploadfile", // 上上上级目录下的uploadfile
                "/uploadfile"                                     // 容器内的绝对路径
            };
            
            System.out.println("尝试查找文件，相对路径: " + filePath);
            System.out.println("当前工作目录: " + System.getProperty("user.dir"));
            
            for (String root : possibleRoots) {
                File testFile = new File(root, filePath);
                System.out.println("  尝试路径: " + testFile.getAbsolutePath() + " (存在: " + testFile.exists() + ")");
                if (testFile.exists()) {
                    file = testFile;
                    System.out.println("  找到文件: " + testFile.getAbsolutePath());
                    break;
                }
            }
            
            if (file == null) {
                throw new RuntimeException("应用文件不存在，已尝试路径: " + filePath + 
                    "，根目录: " + String.join(", ", possibleRoots));
            }
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + app.getFileName() + "\"");
            response.setContentLengthLong(file.length());
            
            // 写入文件内容
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
            }
            
            // 增加下载次数
            incrementDownloadCount(appId);
            
        } catch (Exception e) {
            throw new RuntimeException("下载应用文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void generateQRCode(String appId, jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response) {
        AppEntity app = selectById(appId);
        if (app == null) {
            throw new RuntimeException("应用不存在");
        }
        
        try {
            // 生成完整的下载链接URL，包含协议、域名、端口和context path
            String downloadUrl = app.getDownloadUrl();
            if (downloadUrl == null) {
                downloadUrl = "/app/download/" + appId;
            }
            
            // 构建完整的下载URL
            String fullDownloadUrl = buildFullDownloadUrl(request, downloadUrl);
            
            // 记录日志，便于调试
            System.out.println("生成二维码 - 应用ID: " + appId);
            System.out.println("相对下载路径: " + downloadUrl);
            System.out.println("完整下载URL: " + fullDownloadUrl);
            
            // 生成本地二维码图片 - 使用完整URL
            BufferedImage qrCodeImage = generateQRCodeImage(fullDownloadUrl, 300, 300);
            
            // 将二维码图片转换为Base64字符串
            String base64Image = convertImageToBase64(qrCodeImage);
            
            // 返回JSON响应，包含完整下载URL和Base64编码的二维码图片
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            // 使用更清晰的JSON格式，返回完整URL
            String jsonResponse = String.format(
                "{\"code\":0,\"data\":{\"downloadUrl\":\"%s\",\"imageUrl\":\"data:image/png;base64,%s\"},\"message\":\"二维码生成成功\"}",
                fullDownloadUrl, base64Image
            );
            
            System.out.println("返回的JSON响应: " + jsonResponse);
            response.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            System.err.println("生成二维码失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("生成二维码失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成二维码图片
     */
    private BufferedImage generateQRCodeImage(String text, int width, int height) throws Exception {
        try {
            // 创建二维码写入器
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            
            // 设置二维码参数
            java.util.Map<EncodeHintType, Object> hints = new java.util.HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2);
            
            // 生成二维码矩阵
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            
            // 创建图片
            BufferedImage qrCodeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            
            // 填充图片
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    qrCodeImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            
            return qrCodeImage;
            
        } catch (WriterException e) {
            throw new RuntimeException("生成二维码图片失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将图片转换为Base64字符串
     */
    private String convertImageToBase64(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return java.util.Base64.getEncoder().encodeToString(imageBytes);
        }
    }

    @Override
    public void incrementDownloadCount(String appId) {
        AppEntity app = selectById(appId);
        if (app != null) {
            app.setDownloadCount(app.getDownloadCount() + 1);
            updateById(app);
        }
    }

    @Override
    public void recordDownloadLog(String appId, String userIp, String userAgent) {
        // 这里可以记录下载日志到数据库
        // 暂时只记录到控制台
        System.out.println("下载日志 - 应用ID: " + appId + ", 用户IP: " + userIp + ", 用户代理: " + userAgent);
    }

    /**
     * 构建完整的下载URL
     */
    private String buildFullDownloadUrl(jakarta.servlet.http.HttpServletRequest request, String relativeUrl) {
        if (relativeUrl == null || relativeUrl.trim().isEmpty()) {
            return "";
        }
        
        // 如果已经是完整URL，直接返回
        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
            return relativeUrl;
        }
        
        // 获取请求的协议和域名
        String protocol = request.getScheme();
        String serverName = request.getServerName();
        String contextPath = request.getContextPath();
        
        // 强制使用配置文件中的端口8002，确保二维码中的URL使用正确的端口
        int serverPort = 8002;
        
        // 构建完整URL
        StringBuilder fullUrl = new StringBuilder();
        fullUrl.append(protocol).append("://").append(serverName);
        
        // 添加端口号（8002不是标准端口）
        fullUrl.append(":").append(serverPort);
        
        // 添加context path
        if (contextPath != null && !contextPath.isEmpty()) {
            fullUrl.append(contextPath);
        }
        
        // 添加相对路径
        if (!relativeUrl.startsWith("/")) {
            fullUrl.append("/");
        }
        fullUrl.append(relativeUrl);
        
        // 记录构建的URL信息，便于调试
        System.out.println("构建完整下载URL - 协议: " + protocol + 
                          ", 服务器: " + serverName + 
                          ", 端口: " + serverPort + 
                          ", Context Path: " + contextPath + 
                          ", 相对路径: " + relativeUrl + 
                          ", 完整URL: " + fullUrl.toString());
        
        return fullUrl.toString();
    }
} 