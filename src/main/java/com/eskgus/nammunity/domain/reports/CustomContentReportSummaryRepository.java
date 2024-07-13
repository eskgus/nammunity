package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomContentReportSummaryRepository {
    boolean existsByElement(Element element);
    ContentReportSummary findByElement(Element element);
    Page<ContentReportSummaryDto> findAllDesc(Pageable pageable);
    Page<ContentReportSummaryDto> findByTypes(Types type, Pageable pageable);
    void deleteByElement(Element element);
}
