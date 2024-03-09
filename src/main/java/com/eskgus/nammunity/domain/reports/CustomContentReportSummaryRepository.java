package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;

import java.util.List;

public interface CustomContentReportSummaryRepository {
    <T> boolean existsByContents(T contents);
    <T> ContentReportSummary findByContents(T contents);
    List<ContentReportSummaryDto> findAllDesc();
    List<ContentReportSummaryDto> findByTypes(Types type);
}
