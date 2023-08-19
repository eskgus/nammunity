package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.reports.ReasonsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ReasonsService {
    private final ReasonsRepository reasonsRepository;

    @Transactional(readOnly = true)
    public Reasons findById(Long id) {
        return reasonsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 신고 사유가 없습니다."));
    }
}
