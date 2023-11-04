package com.eskgus.nammunity.web.controller.reports;

import com.eskgus.nammunity.service.reports.ReportsService;
import com.eskgus.nammunity.service.user.BannedUsersService;
import com.eskgus.nammunity.web.dto.reports.ContentReportsDeleteDto;
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
    private final BannedUsersService bannedUsersService;

    @PostMapping("/content")
    public Map<String, String> saveContentReports(@Valid @RequestBody ContentReportsSaveDto requestDto,
                                                    Principal principal) {
        Map<String, String> response = new HashMap<>();

        String username = principal.getName();
        try {
            Long id = reportsService.saveContentReports(requestDto, username);
            response.put("OK", id.toString());
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }

        return response;
    }

    @DeleteMapping("/content/selected-delete")
    public Map<String, String> deleteSelectedContentReports(@RequestBody ContentReportsDeleteDto requestDto) {
        Map<String, String> response = new HashMap<>();

        try {
            reportsService.deleteSelectedContentReports(requestDto);
            response.put("OK", "삭제됐습니다.");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }

        return response;
    }

    @PostMapping("/process")
    public Map<String, String> banUser(@RequestBody Long userId) {
        Map<String, String> response = new HashMap<>();

        try {
            Long id = bannedUsersService.banUser(userId);
            response.put("OK", id.toString());
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }

        return response;
    }
}
