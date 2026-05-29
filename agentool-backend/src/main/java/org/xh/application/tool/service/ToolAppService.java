package org.xh.application.tool.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xh.application.tool.assembler.ToolAssembler;
import org.xh.application.tool.dto.ToolDTO;
import org.xh.application.tool.dto.ToolVersionDTO;
import org.xh.application.tool.service.runtime.ToolEndpointProbeResult;
import org.xh.application.tool.service.runtime.ToolEndpointProbeService;
import org.xh.domain.tool.constant.ToolAuthType;
import org.xh.domain.tool.constant.ToolStatus;
import org.xh.domain.tool.model.ToolEntity;
import org.xh.domain.tool.model.ToolOperationResult;
import org.xh.domain.tool.model.ToolRuntimeConfigEntity;
import org.xh.domain.tool.model.ToolVersionEntity;
import org.xh.domain.tool.model.UserToolCredentialEntity;
import org.xh.domain.tool.model.UserToolEntity;
import org.xh.domain.tool.service.ToolDomainService;
import org.xh.domain.tool.service.ToolRuntimeConfigDomainService;
import org.xh.domain.tool.service.ToolVersionDomainService;
import org.xh.domain.tool.service.UserToolCredentialDomainService;
import org.xh.domain.tool.service.UserToolDomainService;
import org.xh.domain.user.model.UserEntity;
import org.xh.domain.user.service.UserDomainService;
import org.xh.infrastructure.exception.BusinessException;
import org.xh.infrastructure.exception.ParamValidationException;
import org.xh.interfaces.dto.tool.request.CreateToolRequest;
import org.xh.interfaces.dto.tool.request.MarketToolRequest;
import org.xh.interfaces.dto.tool.request.QueryToolRequest;
import org.xh.interfaces.dto.tool.request.ToolRuntimeConfigRequest;
import org.xh.interfaces.dto.tool.request.UpdateToolRequest;
import org.xh.interfaces.dto.tool.request.UserToolCredentialRequest;

/** 工具应用服务 */
@Service
public class ToolAppService {

    private static final Logger logger = LoggerFactory.getLogger(ToolAppService.class);

    private final ToolDomainService toolDomainService;

    private final UserToolDomainService userToolDomainService;

    private final ToolVersionDomainService toolVersionDomainService;

    private final UserDomainService userDomainService;

    private final ToolEndpointProbeService toolEndpointProbeService;

    private final ToolRuntimeConfigDomainService toolRuntimeConfigDomainService;

    private final UserToolCredentialDomainService userToolCredentialDomainService;

    public ToolAppService(ToolDomainService toolDomainService, UserToolDomainService userToolDomainService,
            ToolVersionDomainService toolVersionDomainService, UserDomainService userDomainService,
            ToolEndpointProbeService toolEndpointProbeService,
            ToolRuntimeConfigDomainService toolRuntimeConfigDomainService,
            UserToolCredentialDomainService userToolCredentialDomainService) {
        this.toolDomainService = toolDomainService;
        this.userToolDomainService = userToolDomainService;
        this.toolVersionDomainService = toolVersionDomainService;
        this.userDomainService = userDomainService;
        this.toolEndpointProbeService = toolEndpointProbeService;
        this.toolRuntimeConfigDomainService = toolRuntimeConfigDomainService;
        this.userToolCredentialDomainService = userToolCredentialDomainService;
    }

    /** 上传工具
     * 
     * 业务流程： 1. 将请求转换为实体 2. 调用领域服务创建工具 3. 将实体转换为DTO返回
     *
     * @param request 创建工具请求
     * @param userId 用户ID
     * @return 创建的工具DTO */
    @Transactional
    public ToolDTO uploadTool(CreateToolRequest request, String userId) {
        ToolEndpointProbeResult probeResult = toolEndpointProbeService.probe(request.getRuntimeConfig(),
                request.getName(), request.getDescription());

        // 将请求转换为实体
        ToolEntity toolEntity = ToolAssembler.toEntity(request, userId);
        toolEntity.setStatus(ToolStatus.MANUAL_REVIEW);
        toolEntity.setRuntimeType(request.getRuntimeConfig().getRuntimeType());
        toolEntity.setMcpServerName(probeResult.getMcpServerName());
        // 调用领域服务创建工具
        ToolOperationResult result = toolDomainService.createTool(toolEntity);

        toolRuntimeConfigDomainService
                .saveCurrentConfig(buildRuntimeConfig(request.getRuntimeConfig(), result.getTool().getId(), probeResult));

        // 将实体转换为DTO返回
        return ToolAssembler.toDTO(result.getTool());
    }

    public ToolDTO getToolDetail(String toolId, String userId) {
        ToolEntity toolEntity = toolDomainService.getTool(toolId, userId);

        ToolDTO toolDTO = ToolAssembler.toDTO(toolEntity);
        ToolRuntimeConfigEntity runtimeConfig = toolRuntimeConfigDomainService.findCurrentByToolId(toolId);
        if (runtimeConfig != null) {
            toolDTO.setRuntimeConfig(toRuntimeConfigRequest(runtimeConfig));
        }
        return toolDTO;
    }

    public ToolEndpointProbeResult probeRuntimeConfig(ToolRuntimeConfigRequest request, String toolName,
            String description) {
        return toolEndpointProbeService.probe(request, toolName, description);
    }

    public List<ToolDTO> getUserTools(String userId) {
        List<ToolEntity> toolEntities = toolDomainService.getUserTools(userId);
        return ToolAssembler.toDTOs(toolEntities);
    }

    public ToolDTO updateTool(String toolId, UpdateToolRequest request, String userId) {
        ToolEndpointProbeResult probeResult = toolEndpointProbeService.probe(request.getRuntimeConfig(),
                request.getName(), request.getDescription());

        ToolEntity toolEntity = ToolAssembler.toEntity(request, userId);
        toolEntity.setId(toolId);
        toolEntity.setStatus(ToolStatus.MANUAL_REVIEW);
        toolEntity.setRuntimeType(request.getRuntimeConfig().getRuntimeType());
        toolEntity.setMcpServerName(probeResult.getMcpServerName());
        ToolOperationResult result = toolDomainService.updateTool(toolEntity);

        toolRuntimeConfigDomainService
                .saveCurrentConfig(buildRuntimeConfig(request.getRuntimeConfig(), result.getTool().getId(), probeResult));

        return ToolAssembler.toDTO(result.getTool());
    }

    public void deleteTool(String toolId, String userId) {
        toolDomainService.deleteTool(toolId, userId);
        toolRuntimeConfigDomainService.deleteByToolId(toolId);
    }

    public void marketTool(MarketToolRequest marketToolRequest, String userId) {
        String toolId = marketToolRequest.getToolId();
        String version = marketToolRequest.getVersion();
        ToolEntity toolEntity = toolDomainService.getTool(toolId, userId);
        // 必须是审核通过才能上架
        if (toolEntity.getStatus() != ToolStatus.APPROVED) {
            throw new BusinessException("工具未审核通过，不能上架");
        }

        ToolVersionEntity toolVersionEntity = toolVersionDomainService.findLatestToolVersion(toolId, userId);
        if (toolVersionEntity != null) {
            // 检查版本号是否大于上一个版本
            if (!marketToolRequest.isVersionGreaterThan(toolVersionEntity.getVersion())) {
                throw new ParamValidationException("versionNumber",
                        "新版本号(" + version + ")必须大于当前最新版本号(" + toolVersionEntity.getVersion() + ")");
            }
        }

        // 创建工具版本进行上架
        toolVersionEntity = new ToolVersionEntity();
        BeanUtils.copyProperties(toolEntity, toolVersionEntity);
        toolVersionEntity.setVersion(version);
        toolVersionEntity.setChangeLog(marketToolRequest.getChangeLog());
        toolVersionEntity.setToolId(toolId);
        toolVersionEntity.setPublicStatus(true);
        toolVersionEntity.setId(null);
        toolVersionEntity.setMcpServerName(toolEntity.getMcpServerName());
        toolVersionEntity.setRuntimeType(toolEntity.getRuntimeType());
        toolVersionEntity.setCreatedAt(LocalDateTime.now());
        toolVersionDomainService.addToolVersion(toolVersionEntity);
        toolRuntimeConfigDomainService.snapshotVersion(toolId, version);
    }

    public Page<ToolVersionDTO> marketTools(QueryToolRequest queryToolRequest) {
        Page<ToolVersionEntity> listToolVersion = toolVersionDomainService.listToolVersion(queryToolRequest);
        List<ToolVersionEntity> records = listToolVersion.getRecords();
        Map<String, Long> toolsInstallMap = userToolDomainService
                .getToolsInstall(records.stream().map(ToolVersionEntity::getToolId).toList());
        List<ToolVersionDTO> list = records.stream().map(toolVersionEntity -> {
            ToolVersionDTO toolVersionDTO = ToolAssembler.toDTO(toolVersionEntity);
            toolVersionDTO.setInstallCount(toolsInstallMap.get(toolVersionEntity.getToolId()));
            return toolVersionDTO;
        }).toList();
        Page<ToolVersionDTO> tPage = new Page<>(listToolVersion.getCurrent(), listToolVersion.getSize(),
                listToolVersion.getTotal());

        Map<String, String> userNicknameMap = userDomainService
                .getByIds(list.stream().map(ToolVersionDTO::getUserId).toList()).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getNickname));

        list.forEach(toolVersionDTO -> {
            if (userNicknameMap.containsKey(toolVersionDTO.getUserId())) {
                toolVersionDTO.setUserName(userNicknameMap.get(toolVersionDTO.getUserId()));
            }
        });

        tPage.setRecords(list);
        return tPage;
    }

    public ToolVersionDTO getToolVersionDetail(String toolId, String version, String userId) {
        // 使用带权限验证的方法获取工具版本详情
        ToolVersionEntity toolVersionEntity = toolVersionDomainService.getToolVersion(toolId, version, userId);
        ToolVersionDTO toolVersionDTO = ToolAssembler.toDTO(toolVersionEntity);
        // 设置创建者昵称
        UserEntity userInfo = userDomainService.getUserInfo(toolVersionDTO.getUserId());
        toolVersionDTO.setUserName(userInfo.getNickname());

        // 设置历史版本
        List<ToolVersionEntity> toolVersionEntities = toolVersionDomainService.getToolVersions(toolId, userId);
        toolVersionDTO.setVersions(toolVersionEntities.stream().map(ToolAssembler::toDTO).toList());

        Map<String, Long> toolsInstall = userToolDomainService.getToolsInstall(Arrays.asList(toolId));
        toolVersionDTO.setInstallCount(toolsInstall.get(toolId));
        return toolVersionDTO;
    }

    public void installTool(String toolId, String version, String userId) {
        UserToolEntity userToolEntity = userToolDomainService.findByToolIdAndUserId(toolId, userId);
        // 使用带权限验证的方法获取工具版本
        ToolVersionEntity toolVersionEntity = toolVersionDomainService.getToolVersion(toolId, version, userId);

        if (userToolEntity == null) {
            userToolEntity = new UserToolEntity();
            userToolEntity.setToolId(toolVersionEntity.getToolId());
        }
        String userToolId = userToolEntity.getId();
        BeanUtils.copyProperties(toolVersionEntity, userToolEntity);

        userToolEntity.setUserId(userId);
        userToolEntity.setVersion(toolVersionEntity.getVersion());
        userToolEntity.setId(userToolId);
        userToolEntity.setMcpServerName(toolVersionEntity.getMcpServerName());
        userToolEntity.setRuntimeType(toolVersionEntity.getRuntimeType());
        ToolRuntimeConfigEntity runtimeConfig = toolRuntimeConfigDomainService.findByToolIdAndVersion(toolId, version);
        boolean credentialRequired = runtimeConfig != null && runtimeConfig.getAuthType() != null
                && runtimeConfig.getAuthType() != ToolAuthType.NONE;
        userToolEntity.setCredentialRequired(credentialRequired);
        userToolEntity.setCredentialConfigured(!credentialRequired
                || (userToolEntity.getCredentialConfigured() != null && userToolEntity.getCredentialConfigured()));
        if (userToolEntity.getId() == null) {
            userToolDomainService.add(userToolEntity);
        } else {
            userToolDomainService.update(userToolEntity);
        }
    }

    public Page<ToolVersionDTO> getInstalledTools(String userId, QueryToolRequest queryToolRequest) {

        Page<UserToolEntity> userToolEntityPage = userToolDomainService.listByUserId(userId, queryToolRequest);

        // 查询对应的工具是否还存在
        ArrayList<String> toolIds = new ArrayList<>();

        Map<String, ToolEntity> toolMap = toolDomainService
                .getByIds(userToolEntityPage.getRecords().stream().map(UserToolEntity::getToolId).toList()).stream()
                .collect(Collectors.toMap(ToolEntity::getId, Function.identity()));

        List<ToolVersionDTO> list = userToolEntityPage.getRecords().stream().map(userToolEntity -> {
            ToolVersionDTO dto = ToolAssembler.toDTO(userToolEntity);
            toolIds.add(userToolEntity.getToolId());
            if (!toolMap.containsKey(userToolEntity.getToolId())) {
                dto.setDelete(true);
            }
            return dto;
        }).toList();

        Page<ToolVersionDTO> tPage = new Page<>(userToolEntityPage.getCurrent(), userToolEntityPage.getSize(),
                userToolEntityPage.getTotal());
        tPage.setRecords(list);
        return tPage;
    }

    public List<ToolVersionDTO> getToolVersions(String toolId, String userId) {
        List<ToolVersionEntity> toolVersionEntities = toolVersionDomainService.getToolVersions(toolId, userId);
        return toolVersionEntities.stream().map(ToolAssembler::toDTO).toList();
    }

    public void uninstallTool(String toolId, String userId) {
        // 先检查是否是用户自己创建的工具
        try {
            ToolEntity toolEntity = toolDomainService.getTool(toolId);
            if (toolEntity != null && toolEntity.getUserId().equals(userId)) {
                // 不允许删除用户自己创建的工具
                throw new BusinessException("不允许卸载自己创建的工具");
            }
        } catch (BusinessException e) {
            // 如果原始工具不存在，说明已被删除，允许用户卸载已安装的工具
            logger.info("原始工具不存在，允许用户卸载已安装的工具: toolId={}, userId={}", toolId, userId);
        }

        // 执行正常的卸载流程
        userToolDomainService.delete(toolId, userId);
    }

    public void saveUserToolCredential(String userToolId, UserToolCredentialRequest request, String userId) {
        UserToolEntity userTool = userToolDomainService.findByIdAndUserId(userToolId, userId);
        if (userTool == null) {
            throw new BusinessException("用户工具不存在: " + userToolId);
        }

        UserToolCredentialEntity credential = new UserToolCredentialEntity();
        credential.setUserToolId(userToolId);
        credential.setUserId(userId);
        credential.setToolId(userTool.getToolId());
        credential.setAuthValues(request.getAuthValues());
        userToolCredentialDomainService.save(credential);

        userTool.setCredentialConfigured(true);
        userToolDomainService.update(userTool);
    }

    public List<ToolVersionDTO> getRecommendTools() {
        QueryToolRequest queryToolRequest = new QueryToolRequest();
        queryToolRequest.setPage(1);
        queryToolRequest.setPageSize(Integer.MAX_VALUE);
        Page<ToolVersionEntity> listToolVersion = toolVersionDomainService.listToolVersion(queryToolRequest);
        List<ToolVersionEntity> records = listToolVersion.getRecords();

        Map<String, Long> toolsInstallMap = userToolDomainService
                .getToolsInstall(records.stream().map(ToolVersionEntity::getToolId).toList());

        List<ToolVersionDTO> toolVersionDTOs = records.stream().map(toolVersionEntity -> {
            ToolVersionDTO dto = ToolAssembler.toDTO(toolVersionEntity);
            dto.setInstallCount(toolsInstallMap.get(dto.getToolId()));
            return dto;
        }).toList();

        if (records.size() > 10) {
            // 使用随机数从所有记录中选取10条不重复的记录
            Random random = new Random();
            toolVersionDTOs = toolVersionDTOs.stream().sorted((a, b) -> random.nextInt(2) - 1).limit(10).toList();
        }

        Map<String, String> userNicknameMap = userDomainService
                .getByIds(toolVersionDTOs.stream().map(ToolVersionDTO::getUserId).toList()).stream()
                .collect(Collectors.toMap(UserEntity::getId, UserEntity::getNickname));

        toolVersionDTOs.forEach(toolVersionDTO -> {
            if (userNicknameMap.containsKey(toolVersionDTO.getUserId())) {
                toolVersionDTO.setUserName(userNicknameMap.get(toolVersionDTO.getUserId()));
            }
        });

        return toolVersionDTOs;
    }

    public void updateUserToolVersionStatus(String toolId, String version, Boolean publishStatus, String userId) {
        toolVersionDomainService.updateToolVersionStatus(toolId, version, userId, publishStatus);
    }

    /** 为工具创建者自动安装审核通过的工具
     * @param toolId 工具ID */
    public void autoInstallApprovedTool(String toolId) {
        ToolEntity tool = toolDomainService.getTool(toolId);
        // 确保工具存在且已审核通过
        if (tool == null || tool.getStatus() != ToolStatus.APPROVED) {
            logger.warn("工具ID: {} 不存在或状态不是 APPROVED，无法自动安装。", toolId);
            return;
        }

        String ownerId = tool.getUserId(); // 获取工具创建者ID

        // 检查是否已安装
        UserToolEntity existingInstall = userToolDomainService.findByToolIdAndUserId(toolId, ownerId);
        if (existingInstall != null) {
            logger.info("工具ID: {} 已被用户ID: {} 安装，无需重复自动安装。版本: {}", toolId, ownerId, existingInstall.getVersion());
            return;
        }

        // 尝试查找最新已发布的版本
        ToolVersionEntity versionToInstall = toolVersionDomainService.findLatestToolVersion(toolId, ownerId);

        if (versionToInstall == null) {
            // 没有已发布的版本，为创建者创建一个代表基础配置的、非公开的内部版本记录
            logger.info("工具ID: {} 未找到任何已发布版本，为其创建者 {} 创建一个内部基础版本用于安装。", toolId, ownerId);
            ToolVersionEntity baseVersion = new ToolVersionEntity();
            BeanUtils.copyProperties(tool, baseVersion); // 从ToolEntity复制基础信息
            baseVersion.setId(null); // 确保是新记录
            baseVersion.setToolId(toolId);
            baseVersion.setUserId(ownerId); // 版本归属创建者
            baseVersion.setVersion("0.0.0"); // 特殊版本号
            baseVersion.setChangeLog("Base configuration for owner auto-installation.");
            baseVersion.setPublicStatus(false); // 非公开
            // baseVersion.setInstallCommand(tool.getInstallCommand()); // 如果ToolVersionEntity需要此字段且ToolEntity有，则复制
            baseVersion.setMcpServerName(tool.getMcpServerName());
            baseVersion.setRuntimeType(tool.getRuntimeType());

            // 持久化这个内部基础版本
            toolVersionDomainService.addToolVersion(baseVersion);
            versionToInstall = baseVersion;
            logger.info("工具ID: {} 已成功创建内部基础版本: {}", toolId, versionToInstall.getVersion());
        }

        logger.info("准备为用户ID: {} 安装工具ID: {} 的版本: {}", ownerId, toolId, versionToInstall.getVersion());
        installTool(toolId, versionToInstall.getVersion(), ownerId);
        logger.info("工具ID: {} 版本: {} 已成功为创建者用户ID: {} 自动安装。", toolId, versionToInstall.getVersion(), ownerId);
    }

    // 根据 toolId 获取最新版本
    public ToolVersionDTO getLatestToolVersion(String toolId, String userId) {
        ToolVersionEntity toolVersionEntity = toolVersionDomainService.findLatestToolVersion(toolId, userId);
        return ToolAssembler.toDTO(toolVersionEntity);
    }

    private ToolRuntimeConfigEntity buildRuntimeConfig(ToolRuntimeConfigRequest request, String toolId,
            ToolEndpointProbeResult probeResult) {
        ToolRuntimeConfigEntity config = new ToolRuntimeConfigEntity();
        config.setToolId(toolId);
        config.setRuntimeType(request.getRuntimeType());
        config.setTransportType(request.getTransportType());
        config.setEndpointUrl(request.getEndpointUrl());
        config.setHttpMethod(request.getHttpMethod());
        config.setAuthType(request.getAuthType());
        config.setAuthConfigSchema(request.getAuthConfigSchema());
        config.setDefaultHeaders(request.getDefaultHeaders());
        config.setRequestSchema(request.getRequestSchema());
        config.setResponseSchema(request.getResponseSchema());
        config.setMcpServerName(probeResult.getMcpServerName());
        config.setToolDefinitions(probeResult.getToolDefinitions());
        config.setTimeoutMs(request.getTimeoutMs());
        config.setEnabled(true);
        return config;
    }

    private ToolRuntimeConfigRequest toRuntimeConfigRequest(ToolRuntimeConfigEntity config) {
        ToolRuntimeConfigRequest request = new ToolRuntimeConfigRequest();
        request.setRuntimeType(config.getRuntimeType());
        request.setTransportType(config.getTransportType());
        request.setEndpointUrl(config.getEndpointUrl());
        request.setMcpServerName(config.getMcpServerName());
        request.setHttpMethod(config.getHttpMethod());
        request.setAuthType(config.getAuthType());
        request.setAuthConfigSchema(config.getAuthConfigSchema());
        request.setDefaultHeaders(config.getDefaultHeaders());
        request.setRequestSchema(config.getRequestSchema());
        request.setResponseSchema(config.getResponseSchema());
        request.setTimeoutMs(config.getTimeoutMs());
        return request;
    }
}
