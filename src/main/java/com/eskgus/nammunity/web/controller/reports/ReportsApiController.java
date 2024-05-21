package com.eskgus.nammunity.web.controller.reports;

import com.eskgus.nammunity.service.reports.ReportSummaryService;
import com.eskgus.nammunity.service.reports.ReportsService;
import com.eskgus.nammunity.service.user.BannedUsersService;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDeleteDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportsSaveDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reports")
public class ReportsApiController {
    private final ReportsService reportsService;
    private final BannedUsersService bannedUsersService;
    private final ReportSummaryService reportSummaryService;

    @PostMapping("/content")
    public ResponseEntity<Void> saveContentReports(@Valid @RequestBody ContentReportsSaveDto requestDto,
                                             Principal principal) {
        reportsService.saveContentReports(requestDto, principal);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/content/selected-delete")
    public ResponseEntity<Void> deleteSelectedContentReports(@RequestBody ContentReportSummaryDeleteDto requestDto) {
        reportSummaryService.deleteSelectedReportSummary(requestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/process")
    public ResponseEntity<Void> banUser(@RequestBody Long userId) {
        bannedUsersService.banUser(userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
