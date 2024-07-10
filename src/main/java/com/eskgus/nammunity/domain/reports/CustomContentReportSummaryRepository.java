package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomContentReportSummaryRepository {
    <Contents> boolean existsByContents(Contents contents);
    <Contents> ContentReportSummary findByContents(Contents contents);
    Page<ContentReportSummaryDto> findAllDesc(Pageable pageable);
    Page<ContentReportSummaryDto> findByTypes(Types type, Pageable pageable);
    <Contents> void deleteByContents(Contents contents);
}
