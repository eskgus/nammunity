package com.eskgus.nammunity.web.dto.reports;

import com.eskgus.nammunity.domain.reports.ContentReports;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ContentReportDetailListDto {
    private Long id;
    private String reporter;
    private String reportedDate;
    private String reason;

    @Builder
    public ContentReportDetailListDto(ContentReports report) {
        this.id = report.getId();
        this.reporter = report.getReporter().getNickname();
        this.reportedDate = DateTimeUtil.formatDateTime(report.getCreatedDate());

        String reasonDetail = report.getReasons().getDetail();
        this.reason = reasonDetail.equals("기타") ? reasonDetail + ": " + report.getOtherReasons() : reasonDetail;
    }
}
