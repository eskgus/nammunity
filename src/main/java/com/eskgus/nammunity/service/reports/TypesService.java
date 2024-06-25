package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.reports.TypesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.TYPE_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class TypesService {
    private final TypesRepository typesRepository;

    @Transactional(readOnly = true)
    public Types findByContentType(ContentType contentType) {
        return findByDetail(contentType.getDetail());
    }

    @Transactional(readOnly = true)
    private Types findByDetail(String detail) {
        return typesRepository.findByDetail(detail).orElseThrow(() -> new
                IllegalArgumentException(TYPE_NOT_FOUND.getMessage()));
    }
}
