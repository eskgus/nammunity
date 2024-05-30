package com.eskgus.nammunity.web.controller.mvc.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.service.reports.ReportSummaryService;
import com.eskgus.nammunity.service.reports.ReportsService;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailRequestDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
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
    private final ReportSummaryService reportSummaryService;

    @GetMapping("/content-report")
    public String listContentReports(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        ContentsPageDto<ContentReportSummaryDto> contentsPage = reportSummaryService.findAllDesc(page);
        model.addAttribute("contentsPage", contentsPage);
        return "admin/my-page/content-report";
    }

    @GetMapping("/content-report/posts")
    public String listPostReports(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        ContentsPageDto<ContentReportSummaryDto> contentsPage = reportSummaryService.findByTypes(ContentType.POSTS, page);
        model.addAttribute("contentsPage", contentsPage);
        return "admin/my-page/content-report-posts";
    }

    @GetMapping("/content-report/comments")
    public String listCommentReports(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        ContentsPageDto<ContentReportSummaryDto> contentsPage = reportSummaryService.findByTypes(ContentType.COMMENTS, page);
        model.addAttribute("contentsPage", contentsPage);
        return "admin/my-page/content-report-comments";
    }

    @GetMapping("/content-report/users")
    public String listUserReports(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        ContentsPageDto<ContentReportSummaryDto> contentsPage = reportSummaryService.findByTypes(ContentType.USERS, page);
        model.addAttribute("contentsPage", contentsPage);
        return "admin/my-page/content-report-users";
    }

    @GetMapping("/content-report/details")
    public String listContentReportDetails(@RequestParam(name = "postsId", required = false) Long postId,
                                           @RequestParam(name = "commentsId", required = false) Long commentId,
                                           @RequestParam(name = "userId", required = false) Long userId,
                                           @RequestParam(name = "page", defaultValue = "1") int page,
                                           Model model) {
        ContentReportDetailRequestDto requestDto = ContentReportDetailRequestDto.builder()
                .postId(postId).commentId(commentId).userId(userId).page(page).build();
        ContentReportDetailDto reportDetail = reportsService.listContentReportDetails(requestDto);
        model.addAttribute("reportDetail", reportDetail);
        return "admin/my-page/content-report-details";
    }
}
