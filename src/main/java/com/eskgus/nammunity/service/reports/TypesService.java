package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.reports.TypesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TypesService {
    private final TypesRepository typesRepository;

    @Transactional(readOnly = true)
    public Types findByDetail(String detail) {
        return typesRepository.findByDetail(detail).orElseThrow(() -> new
                IllegalArgumentException("해당 분류가 없습니다."));
    }
}
