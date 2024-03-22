package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomContentReportSummaryRepository {
    <T> boolean existsByContents(T contents);
    <T> ContentReportSummary findByContents(T contents);
    Page<ContentReportSummaryDto> findAllDesc(Pageable pageable);
    Page<ContentReportSummaryDto> findByTypes(Types type, Pageable pageable);
    <T> void deleteByContents(T contents);
}
