package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailListDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomContentReportsRepository {
    <T> User findReporterByContents(T contents);
    <T> LocalDateTime findReportedDateByContents(T contents);
    <T> Reasons findReasonByContents(T contents);
    <T> String findOtherReasonByContents(T contents, Reasons reason);
    <T> List<ContentReportDetailListDto> findByContents(T contents);
    long countReportsByContentTypeAndUser(ContentType contentType, User user);
    <T> long countByContents(T contents);
}
