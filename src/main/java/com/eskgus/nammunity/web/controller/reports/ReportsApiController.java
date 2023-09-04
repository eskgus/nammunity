package com.eskgus.nammunity.web.controller.reports;

import com.eskgus.nammunity.service.reports.ReportsService;
import com.eskgus.nammunity.web.dto.reports.ContentReportsSaveDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reports")
public class ReportsApiController {
    private final ReportsService reportsService;

    @PostMapping("/content")
    public Map<String, String> saveContentReports(@Valid @RequestBody ContentReportsSaveDto requestDto,
                                                    Principal principal) {
        Map<String, String> response = new HashMap<>();

        String username = principal.getName();
        try {
            reportsService.saveContentReports(requestDto, username);
            response.put("OK", "신고되었습니다.");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }

        return response;
    }
}
