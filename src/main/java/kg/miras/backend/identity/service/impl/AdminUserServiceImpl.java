package kg.miras.backend.identity.service.impl;

import java.util.UUID;
import kg.miras.backend.common.exception.BadRequestException;
import kg.miras.backend.common.exception.ForbiddenOperationException;
import kg.miras.backend.common.exception.ResourceNotFoundException;
import kg.miras.backend.identity.dal.entity.AdminUser;
import kg.miras.backend.identity.dal.enums.AdminRole;
import kg.miras.backend.identity.dal.repository.AdminUserRepository;
import kg.miras.backend.identity.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {
    private final AdminUserRepository adminUserRepository;

    @Override
    @Transactional(readOnly = true)
    public void verifySuperAdmin(UUID adminId) {
        if (adminId == null) {
            throw new BadRequestException("adminId is required");
        }
        AdminUser actor = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
        if (actor.getRole() != AdminRole.SUPER_ADMIN) {
            throw new ForbiddenOperationException("Only SUPER_ADMIN can perform this action");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void verifyExists(UUID adminId) {
        if (adminId == null) {
            throw new BadRequestException("adminId is required");
        }
        if (!adminUserRepository.existsById(adminId)) {
            throw new ResourceNotFoundException("Admin user not found");
        }
    }
}
