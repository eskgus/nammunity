package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.reports.ReasonsRepository;
import com.eskgus.nammunity.web.dto.reports.ReasonsListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.REASON_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class ReasonsService {
    private final ReasonsRepository reasonsRepository;

    @Transactional(readOnly = true)
    public Reasons findById(Long id) {
        return reasonsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException(REASON_NOT_FOUND.getMessage()));
    }

    @Transactional(readOnly = true)
    public List<ReasonsListDto> findAllAsc() {
        return reasonsRepository.findAllAsc().stream().map(ReasonsListDto::new).collect(Collectors.toList());
    }
}
