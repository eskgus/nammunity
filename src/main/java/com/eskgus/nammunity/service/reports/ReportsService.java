package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.reports.*;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ReportsService {
    private final ContentReportsRepository contentReportsRepository;
    private final UserService userService;
    private final PostsSearchService postsSearchService;
    private final CommentsSearchService commentsSearchService;
    private final ReasonsService reasonsService;
    private final TypesService typesService;
    private final ReportSummaryService reportSummaryService;

    @Transactional
    public Long saveContentReports(ContentReportsSaveDto requestDto, String username) {
        ContentReportsSaveDto saveDto = createContentReportsSaveDto(requestDto, username);
        Long reportId = contentReportsRepository.save(saveDto.toEntity()).getId();

        // 누적 신고 개수 10개 (or 3개) 이상이면 신고 요약 저장/수정
        if (shouldCreateSummary(saveDto)) {
            createAndSaveSummary(saveDto);
        }

        return reportId;
    }

    private ContentReportsSaveDto createContentReportsSaveDto(ContentReportsSaveDto requestDto, String username) {
        User reporter = userService.findByUsername(username);
        Reasons reasons = reasonsService.findById(requestDto.getReasonsId());
        String otherReasons = requestDto.getOtherReasons();
        if (reasons.getDetail().equals("기타") && (otherReasons == null)) {
            throw new IllegalArgumentException("기타 사유를 입력하세요.");
        }

        Posts posts = null;
        Comments comments = null;
        User user = null;
        ContentType contentType;

        if (requestDto.getPostsId() != null) {
            posts = postsSearchService.findById(requestDto.getPostsId());
            contentType = ContentType.POSTS;
        } else if (requestDto.getCommentsId() != null) {
            comments = commentsSearchService.findById(requestDto.getCommentsId());
            contentType = ContentType.COMMENTS;
        } else if (requestDto.getUserId() != null){
            user = userService.findById(requestDto.getUserId());
            contentType = ContentType.USERS;
        } else {
            throw new IllegalArgumentException("신고 분류가 선택되지 않았습니다.");
        }
        Types types = typesService.findByContentType(contentType);

        return ContentReportsSaveDto.builder()
                .posts(posts).comments(comments).user(user)
                .reporter(reporter).types(types)
                .reasons(reasons).otherReasons(otherReasons)
                .build();
    }

    private <T> boolean shouldCreateSummary(ContentReportsSaveDto saveDto) {
        T contents = getContentsFromReportsSaveDto(saveDto);
        long countByContents = contentReportsRepository.countByContents(contents);

        if (contents instanceof User) {
            return countByContents >= 3;
        }
        return countByContents >= 10;
    }

    private <T> T getContentsFromReportsSaveDto(ContentReportsSaveDto saveDto) {
        if (saveDto.getPosts() != null) {
            return (T) saveDto.getPosts();
        } else if (saveDto.getComments() != null) {
            return (T) saveDto.getComments();
        }
        return (T) saveDto.getUser();
    }

    private void createAndSaveSummary(ContentReportsSaveDto saveDto) {
        ContentReportSummarySaveDto summarySaveDto = createSummarySaveDto(saveDto);
        reportSummaryService.saveOrUpdateContentReportSummary(summarySaveDto);
    }

    private <T> ContentReportSummarySaveDto createSummarySaveDto(ContentReportsSaveDto saveDto) {
        // types, 컨텐츠(posts, comments, user)는 saveDto에서 꺼내서 사용, 나머지는 테이블에서 검색
        T contents = getContentsFromReportsSaveDto(saveDto);

        LocalDateTime reportedDate = contentReportsRepository.findReportedDateByContents(contents);
        User reporter = contentReportsRepository.findReporterByContents(contents);
        Reasons reason = contentReportsRepository.findReasonByContents(contents);
        String otherReason = reason.getDetail().equals("기타") ?
                contentReportsRepository.findOtherReasonByContents(contents, reason) : null;

        return ContentReportSummarySaveDto.builder()
                .posts(saveDto.getPosts()).comments(saveDto.getComments()).user(saveDto.getUser())
                .types(saveDto.getTypes()).reportedDate(reportedDate).reporter(reporter)
                .reasons(reason).otherReasons(otherReason).build();
    }

    @Transactional(readOnly = true)
    public <T> ContentReportDetailDto findDetails(ContentType contentType, Long contentId, int page) {
        T contents = getContentsFromContentId(contentType, contentId);
        return createDetailDto(contentType, contents, page);
    }

    private <T> T getContentsFromContentId(ContentType contentType, Long contentId) {
        if (contentType.equals(ContentType.POSTS)) {
            return (T) postsSearchService.findById(contentId);
        } else if (contentType.equals(ContentType.COMMENTS)) {
            return (T) commentsSearchService.findById(contentId);
        }
        return (T) userService.findById(contentId);
    }

    private <T, U> ContentReportDetailDto createDetailDto(ContentType contentType, T contents, int page) {
        Pageable pageable = PageRequest.of(page - 1, 10);
        Page<ContentReportDetailListDto> reportDetails = contentReportsRepository.findByContents(contents, pageable);
        Types type = typesService.findByContentType(contentType);
        U contentListDto = createContentListDto(contents);
        return ContentReportDetailDto.builder().type(type).contentListDto(contentListDto).reportDetails(reportDetails).build();
    }

    @Transactional(readOnly = true)
    private <T, U> U createContentListDto(T contents) {
        if (contents instanceof Posts) {
            return (U) new PostsListDto((Posts) contents);
        } else if (contents instanceof Comments) {
            return (U) new CommentsListDto((Comments) contents);
        }
        return (U) new UsersListDto((User) contents);
    }
}
