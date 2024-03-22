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
        return null;
    }

    @Override
    public Long extractListDtoId(ContentReportSummaryDto listDto) {
        return listDto.getId();
    }

    @Override
    public ContentReportSummaryDto generateListDto(ContentReportSummary entity) {
        return new ContentReportSummaryDto(entity, entity.getPosts(), entity.getComments(), entity.getUser());
    }

    public Long getTypeId(ContentReportSummary entity) {
        return entity.getTypes().getId();
    }
}
