package com.eskgus.nammunity.domain.reports;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentReportSummaryRepository extends JpaRepository<ContentReportSummary, Long>, CustomContentReportSummaryRepository {
}
