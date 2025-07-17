package xiaozhi.modules.agent.service;

import com.baomidou.mybatisplus.extension.service.IService;

import xiaozhi.modules.agent.entity.AgentTemplateEntity;
import java.util.List;

/**
 * @author chenerlei
 * @description 针对表【ai_agent_template(智能体配置模板表)】的数据库操作Service
 * @createDate 2025-03-22 11:48:18
 */
public interface AgentTemplateService extends IService<AgentTemplateEntity> {

    /**
     * 获取默认模板
     * 
     * @return 默认模板实体
     */
    AgentTemplateEntity getDefaultTemplate();

    /**
     * 获取排序最后一位
     * 
     * @return 默认模板实体
     */
    int getLastSort();

    /**
     * 更新默认模板中的模型ID
     * 
     * @param modelType 模型类型
     * @param modelId   模型ID
     */
    void updateDefaultTemplateModelId(String modelType, String modelId);

    /**
     * 更新默认模板中的模型ID
     * 
     * @param modelType 模型类型
     * @param modelId   模型ID
     */
    List<AgentTemplateEntity> getAllTemplates();

    /**
     * 根据ID获取模板
     * 
     * @param id 模板ID
     * @return 模板实体
     */
    AgentTemplateEntity getTemplateById(String id);

    /**
     * 根据ID删除模板
     * 
     * @param id 模板ID
     */
    void deleteTemplateById(String id);

    /**
     * 根据ID更新模板
     * 
     * @param id 模板ID
     * @param template 模板实体
     */
    void updateTemplateById(String id, AgentTemplateEntity template);

    /**
     * 根据ID创建模板
     * 
     * @param template 模板实体
     */
    void createTemplate(AgentTemplateEntity template);

    /**
     * 根据ID更新模板
     * 
     * @param template 模板实体
     */
    void updateTemplate(AgentTemplateEntity template);

    

}
