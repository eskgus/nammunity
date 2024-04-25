package com.eskgus.nammunity.web.controller.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.service.reports.ReportSummaryService;
import com.eskgus.nammunity.service.reports.ReportsService;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.PaginationDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailListDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

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
        Map<String, Object> attr = new HashMap<>();

        // 게시글 신고 요약 목록
        Page<ContentReportSummaryDto> summaries = reportSummaryService.findByTypes(ContentType.POSTS, page);
        attr.put("summaries", summaries);

        // 페이지 번호
        PaginationDto<ContentReportSummaryDto> paginationDto = PaginationDto.<ContentReportSummaryDto>builder()
                .page(summaries).display(10).build();
        attr.put("pages", paginationDto);

        model.addAllAttributes(attr);
        return "admin/my-page/content-report-posts";
    }

    @GetMapping("/content-report/comments")
    public String listCommentReports(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        Map<String, Object> attr = new HashMap<>();

        // 댓글 신고 요약 목록
        Page<ContentReportSummaryDto> summaries = reportSummaryService.findByTypes(ContentType.COMMENTS, page);
        attr.put("summaries", summaries);

        // 페이지 번호
        PaginationDto<ContentReportSummaryDto> paginationDto = PaginationDto.<ContentReportSummaryDto>builder()
                .page(summaries).display(10).build();
        attr.put("pages", paginationDto);

        model.addAllAttributes(attr);
        return "admin/my-page/content-report-comments";
    }

    @GetMapping("/content-report/users")
    public String listUserReports(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        Map<String, Object> attr = new HashMap<>();

        // 사용자 신고 요약 목록
        Page<ContentReportSummaryDto> summaries = reportSummaryService.findByTypes(ContentType.USERS, page);
        attr.put("summaries", summaries);

        // 페이지 번호
        PaginationDto<ContentReportSummaryDto> paginationDto = PaginationDto.<ContentReportSummaryDto>builder()
                .page(summaries).display(10).build();
        attr.put("pages", paginationDto);

        model.addAllAttributes(attr);
        return "admin/my-page/content-report-users";
    }

    @GetMapping("/content-report/details")
    public String listContentReportDetails(@RequestParam(name = "postsId", required = false) Long postId,
                                           @RequestParam(name = "commentsId", required = false) Long commentId,
                                           @RequestParam(name = "userId", required = false) Long userId,
                                           @RequestParam(name = "page", defaultValue = "1") int page,
                                           Model model) {
        Map<String, Object> attr = new HashMap<>();

        ContentReportDetailDto detail;
        if (postId != null) {
            detail = reportsService.findDetails(ContentType.POSTS, postId, page);
        } else if (commentId != null) {
            detail = reportsService.findDetails(ContentType.COMMENTS, commentId, page);
        } else {
            detail = reportsService.findDetails(ContentType.USERS, userId, page);
        }
        attr.put("detail", detail);

        PaginationDto<ContentReportDetailListDto> paginationDto = PaginationDto.<ContentReportDetailListDto>builder()
                .page(detail.getReportDetails()).display(10).build();
        attr.put("pages", paginationDto);

        model.addAllAttributes(attr);
        return "admin/my-page/content-report-details";
    }
}
