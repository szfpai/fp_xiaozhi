package xiaozhi.modules.app.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("ai_app")
@Schema(description = "应用信息")
public class AppEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用类型")
    private String appType;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "版本码")
    private Integer versionCode;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件名")
    private String fileName;

    @Schema(description = "下载链接")
    private String downloadUrl;

    @Schema(description = "二维码路径")
    private String qrCodePath;

    @Schema(description = "应用描述")
    private String description;

    @Schema(description = "更新日志")
    private String changelog;

    @Schema(description = "是否强制更新")
    private Boolean isForceUpdate;

    @Schema(description = "是否启用")
    private Boolean isActive;

    @Schema(description = "下载次数")
    private Integer downloadCount;

    @Schema(description = "排序")
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private Long creator;

    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

    @TableField(fill = FieldFill.UPDATE)
    private Long updater;

    @TableField(fill = FieldFill.UPDATE)
    private Date updateDate;
} 