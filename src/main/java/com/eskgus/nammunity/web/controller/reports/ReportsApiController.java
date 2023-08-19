package com.eskgus.nammunity.web.controller.reports;

import com.eskgus.nammunity.service.reports.ReportsService;
import com.eskgus.nammunity.web.dto.reports.CommunityReportsSaveDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reports")
public class ReportsApiController {
    private final ReportsService reportsService;

    @PostMapping("/community")
    public Long saveCommunityReports(@Valid @RequestBody CommunityReportsSaveDto requestDto,
                                       Principal principal) {
        String username = principal.getName();
        return reportsService.saveCommunityReports(requestDto, username);
    }
}
