package com.eskgus.nammunity.web.controller.reports;

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

    @GetMapping("/content-report")
    public String listContentReports(Model model) {
        Map<String, Object> attr = new HashMap<>();

        // 전체 신고 요약 목록
        List<ContentReportSummaryDto> summaryDtos = reportsService.findSummary("");
        attr.put("reports", summaryDtos);

        // 전체 신고 요약 개수: List가 null이면 0, 아니면 size
        int numOfReports = (summaryDtos != null) ? summaryDtos.size() : 0;
        attr.put("numOfReports", numOfReports);

        model.addAllAttributes(attr);
        return "admin/my-page/content-report";
    }

    @GetMapping("/content-report/posts")
    public String listPostReports(Model model) {
        Map<String, Object> attr = new HashMap<>();

        // 게시글 신고 요약 목록
        List<ContentReportSummaryDto> summaryDtos = reportsService.findSummary("posts");
        attr.put("reports", summaryDtos);

        // 게시글 신고 요약 개수: List가 null이면 0, 아니면 size
        int numOfReports = (summaryDtos != null) ? summaryDtos.size() : 0;
        attr.put("numOfReports", numOfReports);

        model.addAllAttributes(attr);
        return "admin/my-page/content-report-posts";
    }

    @GetMapping("/content-report/comments")
    public String listCommentReports(Model model) {
        Map<String, Object> attr = new HashMap<>();

        // 댓글 신고 요약 목록
        List<ContentReportSummaryDto> summaryDtos = reportsService.findSummary("comments");
        attr.put("reports", summaryDtos);

        // 댓글 신고 요약 개수: List가 null이면 0, 아니면 size
        int numOfReports = (summaryDtos != null) ? summaryDtos.size() : 0;
        attr.put("numOfReports", numOfReports);

        model.addAllAttributes(attr);
        return "admin/my-page/content-report-comments";
    }

    @GetMapping("/content-report/users")
    public String listUserReports(Model model) {
        Map<String, Object> attr = new HashMap<>();

        // 사용자 신고 요약 목록
        List<ContentReportSummaryDto> summaryDtos = reportsService.findSummary("users");
        attr.put("reports", summaryDtos);

        // 사용자 신고 요약 개수: List가 null이면 0, 아니면 size
        int numOfReports = (summaryDtos != null) ? summaryDtos.size() : 0;
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
            detailDto = reportsService.findDetails("post", postId);
        } else if (commentId != null) {
            detailDto = reportsService.findDetails("comment", commentId);
        } else {
            detailDto = reportsService.findDetails("user", userId);
        }

        model.addAttribute("details", detailDto);
        return "admin/my-page/content-report-details";
    }
}
