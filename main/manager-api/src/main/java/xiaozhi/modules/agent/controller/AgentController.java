package xiaozhi.modules.agent.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.page.PageData;
import xiaozhi.common.redis.RedisKeys;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.user.UserDetail;
import xiaozhi.common.utils.Result;
import xiaozhi.common.utils.ResultUtils;
import xiaozhi.modules.agent.dto.AgentChatHistoryDTO;
import xiaozhi.modules.agent.dto.AgentChatSessionDTO;
import xiaozhi.modules.agent.dto.AgentCreateDTO;
import xiaozhi.modules.agent.dto.AgentDTO;
import xiaozhi.modules.agent.dto.AgentMemoryDTO;
import xiaozhi.modules.agent.dto.AgentUpdateDTO;
import xiaozhi.modules.agent.entity.AgentEntity;
import xiaozhi.modules.agent.entity.AgentTemplateEntity;
import xiaozhi.modules.agent.service.AgentChatAudioService;
import xiaozhi.modules.agent.service.AgentChatHistoryService;
import xiaozhi.modules.agent.service.AgentPluginMappingService;
import xiaozhi.modules.agent.service.AgentService;
import xiaozhi.modules.agent.service.AgentTemplateService;
import xiaozhi.modules.agent.vo.AgentInfoVO;
import xiaozhi.modules.device.entity.DeviceEntity;
import xiaozhi.modules.device.service.DeviceService;
import xiaozhi.modules.security.user.SecurityUser;

@Tag(name = "智能体管理")
@AllArgsConstructor
@RestController
@RequestMapping("/agent")
public class AgentController {
    private final AgentService agentService;
    private final AgentTemplateService agentTemplateService;
    private final DeviceService deviceService;
    private final AgentChatHistoryService agentChatHistoryService;
    private final AgentChatAudioService agentChatAudioService;
    private final AgentPluginMappingService agentPluginMappingService;
    private final RedisUtils redisUtils;

    @GetMapping("/list")
    @Operation(summary = "获取用户智能体列表")
    @RequiresPermissions("sys:role:normal")
    public Result<List<AgentDTO>> getUserAgents() {
        UserDetail user = SecurityUser.getUser();
        List<AgentDTO> agents = agentService.getUserAgents(user.getId());
        return new Result<List<AgentDTO>>().ok(agents);
    }

    @GetMapping("/all")
    @Operation(summary = "智能体列表（管理员）")
    @RequiresPermissions("sys:role:superAdmin")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
    })
    public Result<PageData<AgentEntity>> adminAgentList(
            @Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<AgentEntity> page = agentService.adminAgentList(params);
        return new Result<PageData<AgentEntity>>().ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取智能体详情")
    @RequiresPermissions("sys:role:normal")
    public Result<AgentInfoVO> getAgentById(@PathVariable("id") String id) {
        AgentInfoVO agent = agentService.getAgentById(id);
        return ResultUtils.success(agent);
    }

    @PostMapping
    @Operation(summary = "创建智能体")
    @RequiresPermissions("sys:role:normal")
    public Result<String> save(@RequestBody @Valid AgentCreateDTO dto) {
        String agentId = agentService.createAgent(dto);
        return new Result<String>().ok(agentId);
    }

    @PutMapping("/saveMemory/{macAddress}")
    @Operation(summary = "根据设备id更新智能体")
    public Result<Void> updateByDeviceId(@PathVariable String macAddress, @RequestBody @Valid AgentMemoryDTO dto) {
        DeviceEntity device = deviceService.getDeviceByMacAddress(macAddress);
        if (device == null) {
            return new Result<>();
        }
        AgentUpdateDTO agentUpdateDTO = new AgentUpdateDTO();
        agentUpdateDTO.setSummaryMemory(dto.getSummaryMemory());
        agentService.updateAgentById(device.getAgentId(), agentUpdateDTO);
        return new Result<>();
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新智能体")
    @RequiresPermissions("sys:role:normal")
    public Result<Void> update(@PathVariable String id, @RequestBody @Valid AgentUpdateDTO dto) {
        agentService.updateAgentById(id, dto);
        return new Result<>();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除智能体")
    @RequiresPermissions("sys:role:normal")
    public Result<Void> delete(@PathVariable String id) {
        // 先删除关联的设备
        deviceService.deleteByAgentId(id);
        // 删除关联的聊天记录
        agentChatHistoryService.deleteByAgentId(id, true, true);
        // 删除关联的插件
        agentPluginMappingService.deleteByAgentId(id);
        // 再删除智能体
        agentService.deleteById(id);
        return new Result<>();
    }

    @GetMapping("/template")
    @Operation(summary = "智能体模板模板列表")
    @RequiresPermissions("sys:role:normal")
    public Result<List<AgentTemplateEntity>> templateList() {
        List<AgentTemplateEntity> list = agentTemplateService
                .list(new QueryWrapper<AgentTemplateEntity>().orderByAsc("sort"));
        return new Result<List<AgentTemplateEntity>>().ok(list);
    }
    @GetMapping("/template/{id}")
    @Operation(summary = "提示词模板详情")
    @RequiresPermissions("sys:role:normal")
    public Result<AgentTemplateEntity> getTemplateById(@PathVariable("id") String id) {
        AgentTemplateEntity agentTemplate = agentTemplateService.getTemplateById(id);
        if(agentTemplate == null){
            return new Result<AgentTemplateEntity>().error("模板不存在");
        }
        return new Result<AgentTemplateEntity>().ok(agentTemplate);
    }

    @PutMapping("/updatetemplate")
    @Operation(summary = "更新提示词模板")
    @RequiresPermissions("sys:role:normal")
    public Result<Void> updateTemplateByDto(@RequestBody @Valid AgentTemplateEntity dto) {
        if(dto == null){
            return new Result<Void>().error("参数错误");
        }
        if(StringUtils.isBlank(dto.getId())){
            return new Result<Void>().error("参数错误");
        }
        String id = dto.getId();
        AgentTemplateEntity rawAgentTemplateEntity = agentTemplateService.getTemplateById(id);
        if(rawAgentTemplateEntity == null){
            return new Result<Void>().error("原始模板不存在");
        }
        
        // 逐项对比并赋值
        if(StringUtils.isNotBlank(dto.getAgentCode()) && !dto.getAgentCode().equals(rawAgentTemplateEntity.getAgentCode())){
            rawAgentTemplateEntity.setAgentCode(dto.getAgentCode());
        }
        if(StringUtils.isNotBlank(dto.getAgentName()) && !dto.getAgentName().equals(rawAgentTemplateEntity.getAgentName())){
            rawAgentTemplateEntity.setAgentName(dto.getAgentName());
        }
        if(StringUtils.isNotBlank(dto.getAsrModelId()) && !dto.getAsrModelId().equals(rawAgentTemplateEntity.getAsrModelId())){
            rawAgentTemplateEntity.setAsrModelId(dto.getAsrModelId());
        }
        if(StringUtils.isNotBlank(dto.getVadModelId()) && !dto.getVadModelId().equals(rawAgentTemplateEntity.getVadModelId())){
            rawAgentTemplateEntity.setVadModelId(dto.getVadModelId());
        }
        if(StringUtils.isNotBlank(dto.getLlmModelId()) && !dto.getLlmModelId().equals(rawAgentTemplateEntity.getLlmModelId())){
            rawAgentTemplateEntity.setLlmModelId(dto.getLlmModelId());
        }
        if(StringUtils.isNotBlank(dto.getVllmModelId()) && !dto.getVllmModelId().equals(rawAgentTemplateEntity.getVllmModelId())){
            rawAgentTemplateEntity.setVllmModelId(dto.getVllmModelId());
        }
        if(StringUtils.isNotBlank(dto.getTtsModelId()) && !dto.getTtsModelId().equals(rawAgentTemplateEntity.getTtsModelId())){
            rawAgentTemplateEntity.setTtsModelId(dto.getTtsModelId());
        }
        if(StringUtils.isNotBlank(dto.getTtsVoiceId()) && !dto.getTtsVoiceId().equals(rawAgentTemplateEntity.getTtsVoiceId())){
            rawAgentTemplateEntity.setTtsVoiceId(dto.getTtsVoiceId());
        }
        if(StringUtils.isNotBlank(dto.getMemModelId()) && !dto.getMemModelId().equals(rawAgentTemplateEntity.getMemModelId())){
            rawAgentTemplateEntity.setMemModelId(dto.getMemModelId());
        }
        if(StringUtils.isNotBlank(dto.getIntentModelId()) && !dto.getIntentModelId().equals(rawAgentTemplateEntity.getIntentModelId())){
            rawAgentTemplateEntity.setIntentModelId(dto.getIntentModelId());
        }
        if(dto.getChatHistoryConf() != null && !dto.getChatHistoryConf().equals(rawAgentTemplateEntity.getChatHistoryConf())){
            rawAgentTemplateEntity.setChatHistoryConf(dto.getChatHistoryConf());
        }
        if(StringUtils.isNotBlank(dto.getSystemPrompt()) && !dto.getSystemPrompt().equals(rawAgentTemplateEntity.getSystemPrompt())){
            rawAgentTemplateEntity.setSystemPrompt(dto.getSystemPrompt());
        }
        if(StringUtils.isNotBlank(dto.getSummaryMemory()) && !dto.getSummaryMemory().equals(rawAgentTemplateEntity.getSummaryMemory())){
            rawAgentTemplateEntity.setSummaryMemory(dto.getSummaryMemory());
        }
        if(StringUtils.isNotBlank(dto.getLangCode()) && !dto.getLangCode().equals(rawAgentTemplateEntity.getLangCode())){
            rawAgentTemplateEntity.setLangCode(dto.getLangCode());
        }
        if(StringUtils.isNotBlank(dto.getLanguage()) && !dto.getLanguage().equals(rawAgentTemplateEntity.getLanguage())){
            rawAgentTemplateEntity.setLanguage(dto.getLanguage());
        }
        if(dto.getSort() != null && !dto.getSort().equals(rawAgentTemplateEntity.getSort())){
            rawAgentTemplateEntity.setSort(dto.getSort());
        }
        rawAgentTemplateEntity.setUpdatedAt(new Date());
        agentTemplateService.updateTemplateById(rawAgentTemplateEntity.getId(), rawAgentTemplateEntity);
        return new Result<Void>().ok(null);
    }

    @DeleteMapping("/deletetemplate/{id}")
    @Operation(summary = "删除提示词模板")
    @RequiresPermissions("sys:role:superAdmin")
    public Result<Void> deleteTemplateById(@PathVariable("id") String id) {
        if(StringUtils.isNotBlank(id) && agentTemplateService.getTemplateById(id) != null){
            agentTemplateService.deleteTemplateById(id);
            return new Result<Void>().ok(null);
        }
        return new Result<Void>().error("删除失败，模板不存在");
    }

    @PutMapping("/addtemplate")
    @Operation(summary = "创建提示词模板")
    @RequiresPermissions("sys:role:normal")
    public Result<Void> creatTemplate(@RequestBody @Valid AgentTemplateEntity dto) {
        if (dto != null) {
            AgentTemplateEntity defaultTemplate = agentTemplateService.getDefaultTemplate();
            
            if (StringUtils.isBlank(dto.getId())) {
                dto.setId(UUID.randomUUID().
                toString().
                replace("-", ""));
            }
            if (StringUtils.isBlank(dto.getAgentCode())) {
                dto.setAgentCode(defaultTemplate.getAgentCode());
            }
            if (StringUtils.isBlank(dto.getSystemPrompt())) {
                return new Result<Void>().error("提示词模板内容不能为空");
            }
            if (dto.getSort() == null) {
                dto.setSort(agentTemplateService.getLastSort() + 1);
            }
            if (StringUtils.isBlank(dto.getAgentName())) {
                dto.setAgentName(defaultTemplate.getAgentName());
            }
            if(StringUtils.isBlank(dto.getAsrModelId())){
                dto.setAsrModelId(defaultTemplate.getAsrModelId());
            }
            if(StringUtils.isBlank(dto.getTtsModelId())){
                dto.setTtsModelId(defaultTemplate.getTtsModelId());
            }
            if(StringUtils.isBlank(dto.getVadModelId())){
                dto.setVadModelId(defaultTemplate.getVadModelId());
            }
            if(StringUtils.isBlank(dto.getLlmModelId())){
                dto.setLlmModelId(defaultTemplate.getLlmModelId());
            }
            if(StringUtils.isBlank(dto.getVllmModelId())){
                dto.setVllmModelId(defaultTemplate.getVllmModelId());
            }
            if(StringUtils.isBlank(dto.getTtsVoiceId())){
                dto.setTtsVoiceId(defaultTemplate.getTtsVoiceId());
            }
            if(StringUtils.isBlank(dto.getMemModelId())){
                dto.setMemModelId(defaultTemplate.getMemModelId());
            }
            if(StringUtils.isBlank(dto.getIntentModelId())){
                dto.setIntentModelId(defaultTemplate.getIntentModelId());
            }
            dto.setCreatedAt(new Date());
            dto.setUpdatedAt(new Date());
            agentTemplateService.createTemplate(dto);
            return new Result<Void>().ok(null);
        }
        return new Result<Void>().error("创建失败，请检查模板内容");
    }

    @GetMapping("/{id}/sessions")
    @Operation(summary = "获取智能体会话列表")
    @RequiresPermissions("sys:role:normal")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
    })
    public Result<PageData<AgentChatSessionDTO>> getAgentSessions(
            @PathVariable("id") String id,
            @Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        params.put("agentId", id);
        PageData<AgentChatSessionDTO> page = agentChatHistoryService.getSessionListByAgentId(params);
        return new Result<PageData<AgentChatSessionDTO>>().ok(page);
    }

    @GetMapping("/{id}/chat-history/{sessionId}")
    @Operation(summary = "获取智能体聊天记录")
    @RequiresPermissions("sys:role:normal")
    public Result<List<AgentChatHistoryDTO>> getAgentChatHistory(
            @PathVariable("id") String id,
            @PathVariable("sessionId") String sessionId) {
        // 获取当前用户
        UserDetail user = SecurityUser.getUser();

        // 检查权限
        if (!agentService.checkAgentPermission(id, user.getId())) {
            return new Result<List<AgentChatHistoryDTO>>().error("没有权限查看该智能体的聊天记录");
        }

        // 查询聊天记录
        List<AgentChatHistoryDTO> result = agentChatHistoryService.getChatHistoryBySessionId(id, sessionId);
        return new Result<List<AgentChatHistoryDTO>>().ok(result);
    }

    @PostMapping("/audio/{audioId}")
    @Operation(summary = "获取音频下载ID")
    @RequiresPermissions("sys:role:normal")
    public Result<String> getAudioId(@PathVariable("audioId") String audioId) {
        byte[] audioData = agentChatAudioService.getAudio(audioId);
        if (audioData == null) {
            return new Result<String>().error("音频不存在");
        }
        String uuid = UUID.randomUUID().toString();
        redisUtils.set(RedisKeys.getAgentAudioIdKey(uuid), audioId);
        return new Result<String>().ok(uuid);
    }

    @GetMapping("/play/{uuid}")
    @Operation(summary = "播放音频")
    public ResponseEntity<byte[]> playAudio(@PathVariable("uuid") String uuid) {

        String audioId = (String) redisUtils.get(RedisKeys.getAgentAudioIdKey(uuid));
        if (StringUtils.isBlank(audioId)) {
            return ResponseEntity.notFound().build();
        }

        byte[] audioData = agentChatAudioService.getAudio(audioId);
        if (audioData == null) {
            return ResponseEntity.notFound().build();
        }
        redisUtils.delete(RedisKeys.getAgentAudioIdKey(uuid));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"play.wav\"")
                .body(audioData);
    }

}