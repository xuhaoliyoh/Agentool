package org.xhy.domain.tool.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import org.xhy.domain.tool.model.UserToolCredentialEntity;
import org.xhy.domain.tool.repository.UserToolCredentialRepository;

/** 用户工具凭据领域服务 */
@Service
public class UserToolCredentialDomainService {

    private final UserToolCredentialRepository repository;

    public UserToolCredentialDomainService(UserToolCredentialRepository repository) {
        this.repository = repository;
    }

    public UserToolCredentialEntity findByUserToolId(String userToolId, String userId) {
        LambdaQueryWrapper<UserToolCredentialEntity> wrapper = Wrappers.<UserToolCredentialEntity>lambdaQuery()
                .eq(UserToolCredentialEntity::getUserToolId, userToolId).eq(UserToolCredentialEntity::getUserId, userId)
                .isNull(UserToolCredentialEntity::getDeletedAt);
        return repository.selectOne(wrapper, false);
    }

    public UserToolCredentialEntity save(UserToolCredentialEntity credential) {
        UserToolCredentialEntity existing = findByUserToolId(credential.getUserToolId(), credential.getUserId());
        if (existing == null) {
            repository.checkInsert(credential);
            return credential;
        }
        credential.setId(existing.getId());
        credential.setCreatedAt(existing.getCreatedAt());
        repository.checkedUpdateById(credential);
        return credential;
    }
}
