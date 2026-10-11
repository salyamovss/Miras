package kg.miras.backend.identity.service;

import java.util.UUID;

public interface AdminUserService {
    /**
     * Throws {@link kg.miras.backend.common.exception.ForbiddenOperationException}
     * if the admin is not a SUPER_ADMIN, or
     * {@link kg.miras.backend.common.exception.ResourceNotFoundException} if the
     * admin is not found.
     */
    void verifySuperAdmin(UUID adminId);

    /**
     * Verifies that the admin user exists.
     * Throws {@link kg.miras.backend.common.exception.ResourceNotFoundException}
     * otherwise.
     */
    void verifyExists(UUID adminId);
}
