package com.eskgus.nammunity.domain.reports;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentReportsRepository extends JpaRepository<ContentReports, Long>, CustomContentReportsRepository {
}
