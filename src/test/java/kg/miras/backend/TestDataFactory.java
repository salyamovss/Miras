package kg.miras.backend;

import java.util.UUID;
import kg.miras.backend.catalog.dal.entity.Material;
import kg.miras.backend.catalog.dal.enums.MaterialAccessCondition;
import kg.miras.backend.catalog.dal.enums.MaterialStatus;
import kg.miras.backend.catalog.dal.enums.MaterialType;
import kg.miras.backend.catalog.dal.repository.MaterialRepository;
import kg.miras.backend.identity.dal.entity.AdminUser;
import kg.miras.backend.identity.dal.enums.AdminRole;
import kg.miras.backend.identity.dal.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestDataFactory {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private MaterialRepository materialRepository;

    public AdminUser createAdmin(AdminRole role) {
        AdminUser admin = new AdminUser();
        String random = UUID.randomUUID().toString().substring(0, 8);
        admin.setEmail("admin-" + random + "@miras.test");
        admin.setPasswordHash("hash");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setRole(role);
        admin.setActive(true);
        return adminUserRepository.save(admin);
    }

    public Material createDraftMaterial(UUID createdById) {
        Material material = new Material();
        material.setCreatedById(createdById);
        material.setStatus(MaterialStatus.DRAFT);
        material.setMaterialType(MaterialType.BOOK_RESEARCH);
        material.setPrimaryLanguageCode("ky");
        material.setAccessCondition(MaterialAccessCondition.OPEN_ACCESS);
        return materialRepository.save(material);
    }

    public Material createMaterialWithStatus(UUID createdById, MaterialStatus status) {
        Material material = createDraftMaterial(createdById);
        material.setStatus(status);
        return materialRepository.save(material);
    }
}
