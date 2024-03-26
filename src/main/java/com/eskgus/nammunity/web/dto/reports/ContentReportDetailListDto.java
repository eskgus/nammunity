package com.eskgus.nammunity.web.dto.reports;

import com.eskgus.nammunity.domain.reports.ContentReports;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Getter;

@Getter
public class ContentReportDetailListDto {
    private Long id;
    private String reporter;
    private String reportedDate;
    private String reason;

    public ContentReportDetailListDto(ContentReports report) {
        this.id = report.getId();
        this.reporter = report.getReporter().getNickname();
        this.reportedDate = DateTimeUtil.formatDateTime(report.getCreatedDate());
        this.reason = generateReason(report);
    }

    private String generateReason(ContentReports report) {
        String reasonDetail = report.getReasons().getDetail();
        if (reasonDetail.equals("기타")) {
            return reasonDetail + ": " + report.getOtherReasons();
        }
        return reasonDetail;
    }
}
