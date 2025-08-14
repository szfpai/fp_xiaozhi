package xiaozhi.modules.app.service;

import xiaozhi.common.service.BaseService;
import xiaozhi.common.page.PageData;
import xiaozhi.modules.app.entity.AppEntity;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public interface AppService extends BaseService<AppEntity> {
    
    /**
     * 分页查询应用信息
     */
    PageData<AppEntity> page(Map<String, Object> params);
    
    /**
     * 保存应用信息
     */
    boolean save(AppEntity entity);
    
    /**
     * 更新应用信息
     */
    void update(AppEntity entity);
    
    /**
     * 删除应用
     */
    void delete(String[] ids);
    
    /**
     * 获取最新版本应用
     */
    AppEntity getLatestApp(String appType);
    
    /**
     * 上传应用文件
     */
    AppEntity uploadAppFile(MultipartFile file, String appName, String appType, 
                           String version, Integer versionCode, String description, 
                           String changelog, Boolean isForceUpdate);
    
    /**
     * 下载应用文件
     */
    void downloadApp(String appId, HttpServletResponse response);
    
    /**
     * 生成下载二维码
     */
    void generateQRCode(String appId, jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response);
    
    /**
     * 增加下载次数
     */
    void incrementDownloadCount(String appId);
    
    /**
     * 记录下载日志
     */
    void recordDownloadLog(String appId, String userIp, String userAgent);
} 