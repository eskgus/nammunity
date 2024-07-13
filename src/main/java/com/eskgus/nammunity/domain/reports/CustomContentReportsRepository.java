package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface CustomContentReportsRepository {
    User findReporterByElement(Element element);
    LocalDateTime findReportedDateByElement(Element element);
    Reasons findReasonByElement(Element element);
    String findOtherReasonByElement(Element element, Reasons reason);
    Page<ContentReportDetailListDto> findByElement(Element element, Pageable pageable);
    long countReportsByContentTypeAndUser(ContentType contentType, User user);
    long countByElement(Element element);
}
