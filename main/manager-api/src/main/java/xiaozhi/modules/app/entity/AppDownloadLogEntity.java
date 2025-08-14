package xiaozhi.modules.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_app_download_log")
@Schema(description = "应用下载日志")
public class AppDownloadLogEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "应用ID")
    private String appId;

    @Schema(description = "用户IP")
    private String userIp;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "下载时间")
    private Date downloadTime;

    @Schema(description = "下载状态")
    private Boolean status;
} 