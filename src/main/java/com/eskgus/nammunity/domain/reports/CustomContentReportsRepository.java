package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface CustomContentReportsRepository {
    <Contents> User findReporterByContents(Contents contents);
    <Contents> LocalDateTime findReportedDateByContents(Contents contents);
    <Contents> Reasons findReasonByContents(Contents contents);
    <Contents> String findOtherReasonByContents(Contents contents, Reasons reason);
    <Contents> Page<ContentReportDetailListDto> findByContents(Contents contents, Pageable pageable);
    long countReportsByContentTypeAndUser(ContentType contentType, User user);
    <Contents> long countByContents(Contents contents);
}
