package xiaozhi.modules.app.dao;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import xiaozhi.modules.app.entity.AppEntity;

/**
 * 应用管理DAO
 */
@Mapper
public interface AppDao extends BaseMapper<AppEntity> {
    
} 