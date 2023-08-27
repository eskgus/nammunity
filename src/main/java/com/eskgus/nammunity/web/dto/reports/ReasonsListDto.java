package com.eskgus.nammunity.web.dto.reports;

import com.eskgus.nammunity.domain.reports.Reasons;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReasonsListDto {
    private Long id;
    private String detail;

    @Builder
    public ReasonsListDto(Reasons reasons) {
        this.id = reasons.getId();
        this.detail = reasons.getDetail();
    }
}
