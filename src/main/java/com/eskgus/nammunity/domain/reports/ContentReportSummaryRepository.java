package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContentReportSummaryRepository extends JpaRepository<ContentReportSummary, Long>, CustomContentReportSummaryRepository {
    Optional<ContentReportSummary> findByUser(User user);
}
