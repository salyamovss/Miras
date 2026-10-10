package kg.miras.backend.catalog.service;

import java.util.UUID;
import kg.miras.backend.catalog.dto.request.MaterialDraftRequest;
import kg.miras.backend.catalog.dto.response.MaterialDraftResponse;

public interface MaterialService {
    MaterialDraftResponse saveDraft(UUID adminUserId, MaterialDraftRequest request);
}
