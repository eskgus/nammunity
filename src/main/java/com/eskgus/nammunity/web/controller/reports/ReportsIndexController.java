package com.eskgus.nammunity.web.controller.reports;

import com.eskgus.nammunity.service.reports.ReportsService;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/my-page")
public class ReportsIndexController {
    private final ReportsService reportsService;

    @GetMapping("/content-report")
    public String listContentReports(Model model) {
        model.addAttribute("reports", reportsService.findSummary());
        return "admin/my-page/content-report";
    }

    @GetMapping("/content-report/posts")
    public String listPostReports(Model model) {
        model.addAttribute("reports", reportsService.findSummary());
        return "admin/my-page/content-report-posts";
    }

    @GetMapping("/content-report/comments")
    public String listCommentReports(Model model) {
        model.addAttribute("reports", reportsService.findSummary());
        return "admin/my-page/content-report-comments";
    }

    @GetMapping("/content-report/users")
    public String listUserReports(Model model) {
        model.addAttribute("reports", reportsService.findSummary());
        return "admin/my-page/content-report-users";
    }

    @GetMapping("/content-report/details")
    public String listContentReportDetails(@RequestParam(name = "postId", required = false) Long postId,
                                           @RequestParam(name = "commentId", required = false) Long commentId,
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
