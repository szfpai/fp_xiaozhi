package xiaozhi.modules.app.controller;

import xiaozhi.common.utils.Result;
import xiaozhi.common.page.PageData;
import xiaozhi.modules.app.entity.AppEntity;
import xiaozhi.modules.app.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/app")
@Tag(name = "应用管理")
public class AppController {

    @Autowired
    private AppService appService;

    @GetMapping("/list")
    @Operation(summary = "分页查询应用信息")
    public Result<PageData<AppEntity>> list(@RequestParam Map<String, Object> params) {
        try {
            PageData<AppEntity> page = appService.page(params);
            return new Result<PageData<AppEntity>>().ok(page);
        } catch (Exception e) {
            System.err.println("分页查询应用信息失败: " + e.getMessage());
            e.printStackTrace();
            return new Result<PageData<AppEntity>>().error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    @Operation(summary = "测试数据库连接")
    public Result<Void> testConnection() {
        try {
            // 尝试查询一条记录来测试数据库连接
            PageData<AppEntity> page = appService.page(new HashMap<>());
            return new Result<Void>();
        } catch (Exception e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            e.printStackTrace();
            return new Result<Void>().error("数据库连接失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询应用信息")
    public Result<AppEntity> get(@PathVariable("id") String id) {
        AppEntity entity = appService.selectById(id);
        return new Result<AppEntity>().ok(entity);
    }

    @PostMapping
    @Operation(summary = "保存应用信息")
    public Result<Void> save(@RequestBody AppEntity entity) {
        boolean success = appService.save(entity);
        return success ? new Result<Void>() : new Result<Void>().error("保存失败");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新应用信息")
    public Result<Void> update(@PathVariable("id") String id, @RequestBody AppEntity entity) {
        appService.updateById(entity);
        return new Result<Void>();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除应用")
    public Result<Void> delete(@PathVariable("id") String id) {
        appService.delete(new String[]{id});
        return new Result<Void>();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除应用")
    public Result<Void> deleteBatch(@RequestBody String[] ids) {
        appService.delete(ids);
        return new Result<Void>();
    }

    @GetMapping("/latest/{appType}")
    @Operation(summary = "获取最新版本应用")
    public Result<AppEntity> getLatestApp(@PathVariable("appType") String appType) {
        AppEntity entity = appService.getLatestApp(appType);
        return new Result<AppEntity>().ok(entity);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传应用文件")
    public Result<AppEntity> uploadAppFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("appName") String appName,
            @RequestParam("appType") String appType,
            @RequestParam("version") String version,
            @RequestParam("versionCode") Integer versionCode,
            @RequestParam("description") String description,
            @RequestParam("changelog") String changelog,
            @RequestParam("isForceUpdate") Boolean isForceUpdate) {
        
        AppEntity entity = appService.uploadAppFile(file, appName, appType, version, 
                                                  versionCode, description, changelog, isForceUpdate);
        return new Result<AppEntity>().ok(entity);
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "下载应用文件")
    public void downloadApp(@PathVariable("id") String id, HttpServletResponse response) {
        appService.downloadApp(id, response);
    }

    @GetMapping("/qrcode/{id}")
    @Operation(summary = "生成下载二维码")
    public void generateQRCode(@PathVariable("id") String id, jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response) {
        appService.generateQRCode(id, request, response);
    }

    @PostMapping("/download-count/{id}")
    @Operation(summary = "增加下载次数")
    public Result<Void> incrementDownloadCount(@PathVariable("id") String id) {
        appService.incrementDownloadCount(id);
        return new Result<Void>();
    }

    @PostMapping("/download-log")
    @Operation(summary = "记录下载日志")
    public Result<Void> recordDownloadLog(
            @RequestParam("appId") String appId,
            @RequestParam("userIp") String userIp,
            @RequestParam("userAgent") String userAgent) {
        appService.recordDownloadLog(appId, userIp, userAgent);
        return new Result<Void>();
    }
} 