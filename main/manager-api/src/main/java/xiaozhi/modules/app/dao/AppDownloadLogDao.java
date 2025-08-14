package xiaozhi.modules.app.dao;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xiaozhi.modules.app.entity.AppDownloadLogEntity;

/**
 * 应用下载日志DAO
 */
@Mapper
public interface AppDownloadLogDao extends BaseMapper<AppDownloadLogEntity> {
    
} 