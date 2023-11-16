package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReports;
import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.reports.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReportsService {
    private final ContentReportsRepository contentReportsRepository;
    private final UserService userService;
    private final PostsSearchService postsSearchService;
    private final CommentsSearchService commentsSearchService;
    private final ReasonsService reasonsService;
    private final TypesService typesService;

    @Transactional
    public Long saveContentReports(ContentReportsSaveDto requestDto, String username) {
        User reporter = userService.findByUsername(username);
        Reasons reasons = reasonsService.findById(requestDto.getReasonsId());
        String otherReasons = requestDto.getOtherReasons();
        if (reasons.getDetail().equals("기타") && (otherReasons == null)) {
            throw new IllegalArgumentException("기타 사유를 입력하세요.");
        }

        Posts posts = null;
        Comments comments = null;
        User user = null;
        Types types;

        if (requestDto.getPostsId() != null) {
            posts = postsSearchService.findById(requestDto.getPostsId());
            types = typesService.findByDetail("게시글");
        } else if (requestDto.getCommentsId() != null) {
            comments = commentsSearchService.findById(requestDto.getCommentsId());
            types = typesService.findByDetail("댓글");
        } else if (requestDto.getUserId() != null){
            user = userService.findById(requestDto.getUserId());
            types = typesService.findByDetail("사용자");
        } else {
            throw new IllegalArgumentException("신고 분류가 선택되지 않았습니다.");
        }

        ContentReportsSaveDto contentReportsSaveDto = ContentReportsSaveDto.builder()
                .posts(posts).comments(comments).user(user)
                .reporter(reporter).types(types)
                .reasons(reasons).otherReasons(otherReasons)
                .build();

        return contentReportsRepository.save(contentReportsSaveDto.toEntity()).getId();
    }

    @Transactional(readOnly = true)
    public List<ContentReportSummaryDto> findSummary(String endpoint) {
        List<ContentReportDistinctDto> distinctDtos = findDistinct(endpoint);

        List<ContentReportSummaryDto> summaryDtos = distinctDtos.stream().map(distinctDto -> {
            String type = distinctDto.getTypes().getDetail();
            User reporter;
            LocalDateTime reportedDate;
            Reasons reason;
            String reasonDetail;

            if (type.equals("게시글")) {
                Posts post = distinctDto.getPosts();
                reporter = contentReportsRepository.findReporterByPosts(post);
                reportedDate = contentReportsRepository.findReportedDateByPosts(post);
                reason = contentReportsRepository.findReasonByPosts(post);
                reasonDetail = reason.getDetail();
                if (reasonDetail.equals("기타")) {
                    reasonDetail += ": " + contentReportsRepository.findOtherReasonByPosts(post, reason);
                }
            } else if (type.equals("댓글")) {
                Comments comment = distinctDto.getComments();
                reporter = contentReportsRepository.findReporterByComments(comment);
                reportedDate = contentReportsRepository.findReportedDateByComments(comment);
                reason = contentReportsRepository.findReasonByComments(comment);
                reasonDetail = reason.getDetail();
                if (reasonDetail.equals("기타")) {
                    reasonDetail += ": " + contentReportsRepository.findOtherReasonByComments(comment, reason);
                }
            } else {
                User user = distinctDto.getUser();
                reporter = contentReportsRepository.findReporterByUsers(user);
                reportedDate = contentReportsRepository.findReportedDateByUsers(user);
                reason = contentReportsRepository.findReasonByUsers(user);
                reasonDetail = reason.getDetail();
                if (reasonDetail.equals("기타")) {
                    reasonDetail += ": " + contentReportsRepository.findOtherReasonByUsers(user, reason);
                }
            }

            return ContentReportSummaryDto.builder()
                    .distinctDto(distinctDto)
                    .reporter(reporter)
                    .reportedDate(reportedDate)
                    .reason(reasonDetail)
                    .build();
        }).collect(Collectors.toList());

        return summaryDtos;
    }

    @Transactional(readOnly = true)
    public List<ContentReportDistinctDto> findDistinct(String endpoint) {
        List<ContentReportDistinctDto> distinctDtos = switch (endpoint) {
            case "" -> contentReportsRepository.findDistinct();
            case "posts" -> contentReportsRepository.findDistinctPosts();
            case "comments" -> contentReportsRepository.findDistinctComments();
            default -> contentReportsRepository.findDistinctUsers();
        };

        return distinctDtos;
    }

    @Transactional(readOnly = true)
    public ContentReportDetailDto findDetails(String type, Long id) {
        Posts post = null;
        Comments comment = null;
        User user = null;
        List<ContentReports> reports;

        if (type.equals("post")) {
            post = postsSearchService.findById(id);
            reports = contentReportsRepository.findByPosts(post);
        } else if (type.equals("comment")) {
            comment = commentsSearchService.findById(id);
            reports = contentReportsRepository.findByComments(comment);
        } else {
            user = userService.findById(id);
            reports = contentReportsRepository.findByUser(user);
        }

        // 신고 번호, 신고자, 신고일, 신고 사유 목록
        List<ContentReportDetailListDto> detailListDtos = reports.stream().map(report -> {
            String reason = report.getReasons().getDetail();
            if (report.getReasons().getDetail().equals("기타")) {
                reason += ": " + contentReportsRepository.findOtherReasonById(report.getId());
            }
            return ContentReportDetailListDto.builder().report(report).reason(reason).build();
        }).collect(Collectors.toList());

        // 신고 세부 내역 개수: List가 null이면 0, 아니면 size
        int numOfReports = (detailListDtos != null) ? detailListDtos.size() : 0;

        // 신고 분류, 신고 내용, 세부 목록
        return ContentReportDetailDto.builder()
                .post(post).comment(comment).user(user)
                .detailListDtos(detailListDtos).numOfReports(numOfReports).build();
    }

    @Transactional
    public void deleteSelectedContentReports(ContentReportsDeleteDto requestDto) {
        if (requestDto.getPostsId().isEmpty()
                && requestDto.getCommentsId().isEmpty()
                && requestDto.getUserId().isEmpty()) {
            throw new IllegalArgumentException("삭제할 항목을 선택하세요.");
        }

        requestDto.getPostsId().forEach(postId -> {
            Posts post = postsSearchService.findById(postId);
            contentReportsRepository.deleteByPost(post);
        });
        requestDto.getCommentsId().forEach(commentId -> {
            Comments comment = commentsSearchService.findById(commentId);
            contentReportsRepository.deleteByComment(comment);
        });
        requestDto.getUserId().forEach(userId -> {
            User user = userService.findById(userId);
            contentReportsRepository.deleteByUsers(user);
        });
    }
}
