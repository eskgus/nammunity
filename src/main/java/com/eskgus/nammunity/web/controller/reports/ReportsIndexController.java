package com.eskgus.nammunity.web.controller.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.service.reports.ReportSummaryService;
import com.eskgus.nammunity.service.reports.ReportsService;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/my-page")
public class ReportsIndexController {
    private final ReportsService reportsService;
    private final ReportSummaryService reportSummaryService;

    @GetMapping("/content-report")
    public String listContentReports(Model model) {
        Map<String, Object> attr = new HashMap<>();

        // 전체 신고 요약 목록
        List<ContentReportSummaryDto> summaryDtos = reportSummaryService.findAllDesc();
        attr.put("reports", summaryDtos);

        // 전체 신고 요약 개수
        int numOfReports = summaryDtos.size();
        attr.put("numOfReports", numOfReports);

        model.addAllAttributes(attr);
        return "admin/my-page/content-report";
    }

    @GetMapping("/content-report/posts")
    public String listPostReports(Model model) {
        Map<String, Object> attr = new HashMap<>();

        // 게시글 신고 요약 목록
        List<ContentReportSummaryDto> summaryDtos = reportSummaryService.findByTypes(ContentType.POSTS);
        attr.put("reports", summaryDtos);

        // 게시글 신고 요약 개수
        int numOfReports = summaryDtos.size();
        attr.put("numOfReports", numOfReports);

        model.addAllAttributes(attr);
        return "admin/my-page/content-report-posts";
    }

    @GetMapping("/content-report/comments")
    public String listCommentReports(Model model) {
        Map<String, Object> attr = new HashMap<>();

        // 댓글 신고 요약 목록
        List<ContentReportSummaryDto> summaryDtos = reportSummaryService.findByTypes(ContentType.COMMENTS);
        attr.put("reports", summaryDtos);

        // 댓글 신고 요약 개수
        int numOfReports = summaryDtos.size();
        attr.put("numOfReports", numOfReports);

        model.addAllAttributes(attr);
        return "admin/my-page/content-report-comments";
    }

    @GetMapping("/content-report/users")
    public String listUserReports(Model model) {
        Map<String, Object> attr = new HashMap<>();

        // 사용자 신고 요약 목록
        List<ContentReportSummaryDto> summaryDtos = reportSummaryService.findByTypes(ContentType.USERS);
        attr.put("reports", summaryDtos);

        // 사용자 신고 요약 개수
        int numOfReports = summaryDtos.size();
        attr.put("numOfReports", numOfReports);

        model.addAllAttributes(attr);
        return "admin/my-page/content-report-users";
    }

    @GetMapping("/content-report/details")
    public String listContentReportDetails(@RequestParam(name = "postsId", required = false) Long postId,
                                           @RequestParam(name = "commentsId", required = false) Long commentId,
                                           @RequestParam(name = "userId", required = false) Long userId,
                                           Model model) {
        ContentReportDetailDto detailDto;
        if (postId != null) {
            detailDto = reportsService.findDetails(ContentType.POSTS, postId);
        } else if (commentId != null) {
            detailDto = reportsService.findDetails(ContentType.COMMENTS, commentId);
        } else {
            detailDto = reportsService.findDetails(ContentType.USERS, userId);
        }

        model.addAttribute("details", detailDto);
        return "admin/my-page/content-report-details";
    }
}
