package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface CustomContentReportsRepository {
    <T> User findReporterByContents(T contents);
    <T> LocalDateTime findReportedDateByContents(T contents);
    <T> Reasons findReasonByContents(T contents);
    <T> String findOtherReasonByContents(T contents, Reasons reason);
    <T> Page<ContentReportDetailListDto> findByContents(T contents, Pageable pageable);
    long countReportsByContentTypeAndUser(ContentType contentType, User user);
    <T> long countByContents(T contents);
}
