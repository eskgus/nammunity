package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.reports.ReportsVisitor;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.util.PaginationRepoUtil;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.reports.*;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.EMPTY_OTHER_REASON;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.EMPTY_TYPE;
import static com.eskgus.nammunity.domain.enums.Fields.OTHER;

@RequiredArgsConstructor
@Service
public class ReportsService {
    private final ContentReportsRepository contentReportsRepository;
    private final UserService userService;
    private final PostsService postsService;
    private final CommentsService commentsService;
    private final ReasonsService reasonsService;
    private final TypesService typesService;
    private final ReportSummaryService reportSummaryService;
    private final PrincipalHelper principalHelper;

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

    @Transactional(readOnly = true)
    public ContentReportDetailDto listContentReportDetails(ContentReportDetailRequestDto requestDto) {
        int page = requestDto.getPage();

        if (requestDto.getPostId() != null) {
            Posts post = findPostsById(requestDto.getPostId());
            return createContentReportDetailDto(post, new PostsListDto(post), ContentType.POSTS, page);
        } else if (requestDto.getCommentId() != null) {
            Comments comment = findCommentsById(requestDto.getCommentId());
            return createContentReportDetailDto(comment, new CommentsListDto(comment), ContentType.COMMENTS, page);
        } else {
            User user = findUsersById(requestDto.getUserId());
            return createContentReportDetailDto(user, new UsersListDto(user), ContentType.USERS, page);
        }
    }

    @Transactional(readOnly = true)
    public Long countReportsByContentTypeAndUser(ContentType contentType, User user) {
        return contentReportsRepository.countReportsByContentTypeAndUser(contentType, user);
    }

    private ContentReportsSaveDto createContentReportsSaveDto(ContentReportsSaveDto requestDto, Principal principal) {
        User reporter = principalHelper.getUserFromPrincipal(principal, true);
        Reasons reasons = reasonsService.findById(requestDto.getReasonsId());
        String otherReasons = requestDto.getOtherReasons();
        if (OTHER.getKey().equals(reasons.getDetail()) && (otherReasons == null)) {
            throw new IllegalArgumentException(EMPTY_OTHER_REASON.getMessage());
        }

        Posts posts = null;
        Comments comments = null;
        User user = null;
        ContentType contentType;

        if (requestDto.getPostsId() != null) {
            posts = findPostsById(requestDto.getPostsId());
            contentType = ContentType.POSTS;
        } else if (requestDto.getCommentsId() != null) {
            comments = findCommentsById(requestDto.getCommentsId());
            contentType = ContentType.COMMENTS;
        } else if (requestDto.getUserId() != null){
            user = findUsersById(requestDto.getUserId());
            contentType = ContentType.USERS;
        } else {
            throw new IllegalArgumentException(EMPTY_TYPE.getMessage());
        }
        Types types = findTypesByContentType(contentType);

        return ContentReportsSaveDto.builder()
                .posts(posts).comments(comments).user(user)
                .reporter(reporter).types(types)
                .reasons(reasons).otherReasons(otherReasons)
                .build();
    }

    private boolean shouldCreateSummary(ContentReportsSaveDto saveDto) {
        Element element = getElementFromReportsSaveDto(saveDto);

        long countByContents = contentReportsRepository.countByElement(element);

        ReportsVisitor visitor = new ReportsVisitor();
        element.accept(visitor);

        if (visitor.isUser()) {
            return countByContents >= 3;
        } else {
            return countByContents >= 10;
        }
    }

    private void createAndSaveSummary(ContentReportsSaveDto saveDto) {
        ContentReportSummarySaveDto summarySaveDto = createSummarySaveDto(saveDto);
        reportSummaryService.saveOrUpdateContentReportSummary(summarySaveDto);
    }

    private ContentReportSummarySaveDto createSummarySaveDto(ContentReportsSaveDto saveDto) {
        // types, 컨텐츠(posts, comments, user)는 saveDto에서 꺼내서 사용, 나머지는 테이블에서 검색
        Element element = getElementFromReportsSaveDto(saveDto);

        LocalDateTime reportedDate = contentReportsRepository.findReportedDateByElement(element);
        User reporter = contentReportsRepository.findReporterByElement(element);
        Reasons reason = contentReportsRepository.findReasonByElement(element);
        String otherReason = OTHER.getKey().equals(reason.getDetail()) ?
                contentReportsRepository.findOtherReasonByElement(element, reason) : null;

        return ContentReportSummarySaveDto.builder()
                .posts(saveDto.getPosts()).comments(saveDto.getComments()).user(saveDto.getUser())
                .types(saveDto.getTypes()).reportedDate(reportedDate).reporter(reporter)
                .reasons(reason).otherReasons(otherReason).build();
    }

    private Element getElementFromReportsSaveDto(ContentReportsSaveDto saveDto) {
        if (saveDto.getPosts() != null) {
            return saveDto.getPosts();
        } else if (saveDto.getComments() != null) {
            return saveDto.getComments();
        } else {
            return saveDto.getUser();
        }
    }

    private <Dto> ContentReportDetailDto<Dto> createContentReportDetailDto(Element element, Dto dto,
                                                                           ContentType contentType, int page) {
        Types type = findTypesByContentType(contentType);
        ContentsPageDto<ContentReportDetailListDto> contentsPageDto = createContentsPageDto(page, element);

        return ContentReportDetailDto.<Dto>builder()
                .type(type).dto(dto).contentsPage(contentsPageDto).build();
    }

    private Posts findPostsById(Long postId) {
        return postsService.findById(postId);
    }

    private Comments findCommentsById(Long commentId) {
        return commentsService.findById(commentId);
    }

    private User findUsersById(Long userId) {
        return userService.findById(userId);
    }

    private Types findTypesByContentType(ContentType contentType) {
        return typesService.findByContentType(contentType);
    }

    private ContentsPageDto<ContentReportDetailListDto> createContentsPageDto(int page, Element element) {
        Pageable pageable = PaginationRepoUtil.createPageable(page, 10);
        Page<ContentReportDetailListDto> reportsPage = contentReportsRepository.findByElement(element, pageable);

        return new ContentsPageDto<>(reportsPage);
    }
}
