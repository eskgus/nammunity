package com.eskgus.nammunity.converter;

import com.eskgus.nammunity.domain.reports.ContentReportSummary;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;

public class ContentReportSummaryConverterForTest implements EntityConverterForTest<ContentReportSummary, ContentReportSummaryDto> {
    @Override
    public Long extractEntityId(ContentReportSummary entity) {
        return entity.getId();
    }

    @Override
    public Long extractUserId(ContentReportSummary entity) {
        return entity.getUser().getId();
    }

    @Override
    public Long extractDtoId(ContentReportSummaryDto dto) {
        return dto.getId();
    }

    @Override
    public ContentReportSummaryDto generateDto(ContentReportSummary entity) {
        return new ContentReportSummaryDto(entity, entity.getPosts(), entity.getComments(), entity.getUser());
    }

    public Long extractTypeId(ContentReportSummary entity) {
        return entity.getTypes().getId();
    }
}
