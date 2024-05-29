package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.reports.*;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;

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

    @Autowired
    private PrincipalHelper principalHelper;

    @Transactional
    public Long saveContentReports(ContentReportsSaveDto requestDto, Principal principal) {
        ContentReportsSaveDto saveDto = createContentReportsSaveDto(requestDto, principal);
        Long reportId = contentReportsRepository.save(saveDto.toEntity()).getId();

        // 누적 신고 개수 10개 (or 3개) 이상이면 신고 요약 저장/수정
        if (shouldCreateSummary(saveDto)) {
            createAndSaveSummary(saveDto);
        }

        return reportId;
    }

    private ContentReportsSaveDto createContentReportsSaveDto(ContentReportsSaveDto requestDto, Principal principal) {
        User reporter = principalHelper.getUserFromPrincipal(principal, true);
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
    public ContentReportDetailDto listContentReportDetails(ContentReportDetailRequestDto requestDto) {
        int page = requestDto.getPage();

        if (requestDto.getPostId() != null) {
            Posts post = postsSearchService.findById(requestDto.getPostId());
            return createContentReportDetailDto(post, new PostsListDto(post), ContentType.POSTS, page);
        } else if (requestDto.getCommentId() != null) {
            Comments comment = commentsSearchService.findById(requestDto.getCommentId());
            return createContentReportDetailDto(comment, new CommentsListDto(comment), ContentType.COMMENTS, page);
        } else {
            User user = userService.findById(requestDto.getUserId());
            return createContentReportDetailDto(user, new UsersListDto(user), ContentType.USERS, page);
        }
    }

    private <T, U> ContentReportDetailDto<U> createContentReportDetailDto(T content, U contentListDto,
                                                                       ContentType contentType, int page) {
        Types type = typesService.findByContentType(contentType);
        ContentsPageDto<ContentReportDetailListDto> contentsPage = createContentsPage(page, content);
        return ContentReportDetailDto.<U>builder()
                .type(type).contentListDto(contentListDto).contentsPage(contentsPage).build();
    }

    private <T> ContentsPageDto<ContentReportDetailListDto> createContentsPage(int page, T content) {
        Pageable pageable = createPageable(page, 10);
        Page<ContentReportDetailListDto> contents = contentReportsRepository.findByContents(content, pageable);
        return new ContentsPageDto<>(contents);
    }

    @Transactional(readOnly = true)
    public Long countReportsByContentTypeAndUser(ContentType contentType, User user) {
        return contentReportsRepository.countReportsByContentTypeAndUser(contentType, user);
    }
}
