package org.xh.domain.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.xh.domain.tool.model.ToolRuntimeConfigEntity;
import org.xh.domain.tool.repository.ToolRuntimeConfigRepository;
import org.xh.infrastructure.exception.BusinessException;

import java.time.LocalDateTime;
import java.util.List;

/** 工具运行配置领域服务 */
@Service
public class ToolRuntimeConfigDomainService {

    private final ToolRuntimeConfigRepository repository;

    public ToolRuntimeConfigDomainService(ToolRuntimeConfigRepository repository) {
        this.repository = repository;
    }

    public ToolRuntimeConfigEntity saveCurrentConfig(ToolRuntimeConfigEntity config) {
        if (config == null || config.getToolId() == null) {
            throw new BusinessException("工具运行配置不能为空");
        }
        config.setToolVersion(null);
        config.setEnabled(config.getEnabled() == null ? true : config.getEnabled());
        config.setTimeoutMs(config.getTimeoutMs() == null ? 30000 : config.getTimeoutMs());

        ToolRuntimeConfigEntity existing = findCurrentByToolId(config.getToolId());
        if (existing == null) {
            repository.checkInsert(config);
            return config;
        }

        config.setId(existing.getId());
        config.setCreatedAt(existing.getCreatedAt());
        config.setUpdatedAt(LocalDateTime.now());
        repository.checkedUpdateById(config);
        return config;
    }

    public ToolRuntimeConfigEntity snapshotVersion(String toolId, String version) {
        ToolRuntimeConfigEntity current = findCurrentByToolId(toolId);
        if (current == null) {
            throw new BusinessException("工具运行配置不存在: " + toolId);
        }

        ToolRuntimeConfigEntity snapshot = copy(current);
        snapshot.setId(null);
        snapshot.setToolVersion(version);
        repository.checkInsert(snapshot);
        return snapshot;
    }

    public ToolRuntimeConfigEntity findCurrentByToolId(String toolId) {
        LambdaQueryWrapper<ToolRuntimeConfigEntity> wrapper = Wrappers.<ToolRuntimeConfigEntity>lambdaQuery()
                .eq(ToolRuntimeConfigEntity::getToolId, toolId).isNull(ToolRuntimeConfigEntity::getToolVersion)
                .eq(ToolRuntimeConfigEntity::getEnabled, true).isNull(ToolRuntimeConfigEntity::getDeletedAt)
                .orderByDesc(ToolRuntimeConfigEntity::getUpdatedAt);
        return repository.selectOne(wrapper, false);
    }

    public ToolRuntimeConfigEntity findByToolIdAndVersion(String toolId, String version) {
        if (version == null || version.trim().isEmpty() || "0.0.0".equals(version)) {
            return findCurrentByToolId(toolId);
        }
        LambdaQueryWrapper<ToolRuntimeConfigEntity> wrapper = Wrappers.<ToolRuntimeConfigEntity>lambdaQuery()
                .eq(ToolRuntimeConfigEntity::getToolId, toolId)
                .eq(ToolRuntimeConfigEntity::getToolVersion, version).eq(ToolRuntimeConfigEntity::getEnabled, true)
                .isNull(ToolRuntimeConfigEntity::getDeletedAt).orderByDesc(ToolRuntimeConfigEntity::getUpdatedAt);
        ToolRuntimeConfigEntity config = repository.selectOne(wrapper, false);
        return config != null ? config : findCurrentByToolId(toolId);
    }

    public void deleteByToolId(String toolId) {
        LambdaUpdateWrapper<ToolRuntimeConfigEntity> wrapper = Wrappers.<ToolRuntimeConfigEntity>lambdaUpdate()
                .eq(ToolRuntimeConfigEntity::getToolId, toolId).isNull(ToolRuntimeConfigEntity::getDeletedAt)
                .set(ToolRuntimeConfigEntity::getDeletedAt, LocalDateTime.now())
                .set(ToolRuntimeConfigEntity::getEnabled, false);
        repository.update(null, wrapper);
    }

    public List<ToolRuntimeConfigEntity> findByToolIds(List<String> toolIds) {
        if (toolIds == null || toolIds.isEmpty()) {
            return List.of();
        }
        return repository.selectList(Wrappers.<ToolRuntimeConfigEntity>lambdaQuery()
                .in(ToolRuntimeConfigEntity::getToolId, toolIds).isNull(ToolRuntimeConfigEntity::getToolVersion)
                .isNull(ToolRuntimeConfigEntity::getDeletedAt));
    }

    private ToolRuntimeConfigEntity copy(ToolRuntimeConfigEntity source) {
        ToolRuntimeConfigEntity target = new ToolRuntimeConfigEntity();
        target.setToolId(source.getToolId());
        target.setRuntimeType(source.getRuntimeType());
        target.setTransportType(source.getTransportType());
        target.setEndpointUrl(source.getEndpointUrl());
        target.setHttpMethod(source.getHttpMethod());
        target.setAuthType(source.getAuthType());
        target.setAuthConfigSchema(source.getAuthConfigSchema());
        target.setDefaultHeaders(source.getDefaultHeaders());
        target.setRequestSchema(source.getRequestSchema());
        target.setResponseSchema(source.getResponseSchema());
        target.setToolDefinitions(source.getToolDefinitions());
        target.setMcpServerName(source.getMcpServerName());
        target.setTimeoutMs(source.getTimeoutMs());
        target.setEnabled(source.getEnabled());
        return target;
    }
}
