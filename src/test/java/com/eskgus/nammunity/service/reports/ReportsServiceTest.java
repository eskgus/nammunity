package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.domain.common.Visitor;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.reports.*;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.eskgus.nammunity.domain.enums.ContentType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportsServiceTest {
    @Mock
    private ContentReportsRepository contentReportsRepository;

    @Mock
    private UserService userService;

    @Mock
    private PostsService postsService;

    @Mock
    private CommentsService commentsService;

    @Mock
    private ReasonsService reasonsService;

    @Mock
    private TypesService typesService;

    @Mock
    private ReportSummaryService reportSummaryService;

    @Mock
    private PrincipalHelper principalHelper;

    @InjectMocks
    private ReportsService reportsService;

    private static final Long ID = 1L;
    private static final Long COUNT_ZERO = 0L;
    private static final Long COUNT_TEN = 10L;
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    public void savePostReportsOnly() {
        Posts post = givePost();
        testSaveContentReports(POSTS, post, COUNT_ZERO);
    }

    @Test
    public void saveCommentReportsOnly() {
        Comments comment = giveComment();
        testSaveContentReports(COMMENTS, comment, COUNT_ZERO);
    }

    @Test
    public void saveUserReportsOnly() {
        User user = giveUser();
        testSaveContentReports(USERS, user, COUNT_ZERO);
    }

    @Test
    public void savePostReportsAndSummary() {
        Posts post = givePost();

        testSaveContentReports(POSTS, post, COUNT_TEN);
    }

    @Test
    public void saveCommentReportsAndSummary() {
        Comments comment = giveComment();
        testSaveContentReports(COMMENTS, comment, COUNT_TEN);
    }

    @Test
    public void saveUserReportsAndSummary() {
        User user = giveUser();
        testSaveContentReports(USERS, user, 3L);
    }

    @Test
    public void listPostReportDetails() {
        Posts post = givePost();
        User user = mock(User.class);
        when(post.getUser()).thenReturn(user);
        when(post.getModifiedDate()).thenReturn(NOW);

        ContentReportDetailDto<PostsListDto> result = testListContentReportDetails(POSTS, post);

        assertEquals(post.getId(), result.getPostsListDto().getId());
    }

    @Test
    public void listCommentReportDetails() {
        Comments comment = giveComment();
        User user = mock(User.class);
        when(comment.getUser()).thenReturn(user);
        when(comment.getModifiedDate()).thenReturn(NOW);

        Posts post = mock(Posts.class);
        when(comment.getPosts()).thenReturn(post);

        ContentReportDetailDto<CommentsListDto> result = testListContentReportDetails(COMMENTS, comment);

        assertEquals(comment.getId(), result.getCommentsListDto().getCommentsId());
    }

    @Test
    public void listUserReportDetails() {
        User user = giveUser();
        when(user.getCreatedDate()).thenReturn(NOW);

        ContentReportDetailDto<UsersListDto> result = testListContentReportDetails(USERS, user);

        assertEquals(user.getId(), result.getUsersListDto().getId());
    }

    @Test
    public void countReportsByContentTypeAndUser() {
        // given
        ContentType contentType = POSTS;

        User user = mock(User.class);

        when(contentReportsRepository.countReportsByContentTypeAndUser(any(ContentType.class), any(User.class)))
                .thenReturn(COUNT_TEN);

        // when
        Long result = reportsService.countReportsByContentTypeAndUser(contentType, user);

        // then
        assertEquals(COUNT_TEN, result);

        verify(contentReportsRepository).countReportsByContentTypeAndUser(eq(contentType), eq(user));
    }

    private void testSaveContentReports(ContentType contentType, Element content, long count) {
        // given
        ContentReportsSaveDto requestDto = createReportsSaveDto(contentType);

        Pair<Principal, User> pair = givePrincipal();
        Principal principal = pair.getFirst();
        User reporter = pair.getSecond();

        Reasons reason = giveReason();

        giveType();

        ContentReports report = giveReport();

        when(contentReportsRepository.countByElement(any())).thenReturn(count);

        giveVisitor(contentType, content);

        List<Long> contentIds = Arrays.asList(
                requestDto.getPostsId(), requestDto.getCommentsId(), requestDto.getUserId());

        VerificationMode mode;
        if (count > 0) {
            giveSummarySaveDto(reporter, reason);
            mode = times(1);
        } else {
            mode = never();
        }

        // when
        Long result = reportsService.saveContentReports(requestDto, principal);

        // then
        assertEquals(report.getId(), result);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(reasonsService).findById(eq(requestDto.getReasonsId()));
        verifyFindContentById(contentType, contentIds);
        verify(typesService).findByContentType(eq(contentType));
        verify(contentReportsRepository).save(any(ContentReports.class));
        verify(contentReportsRepository).countByElement(eq(content));
        verifyAfterCountByContents(mode, content);
    }

    private <Dto> ContentReportDetailDto<Dto> testListContentReportDetails(ContentType contentType, Element content) {
        // given
        ContentReportDetailRequestDto requestDto = createReportDetailRequestDto(contentType);

        Types type = giveType();
        when(type.getDetail()).thenReturn(contentType.getDetail());

        Page<ContentReportDetailListDto> reportDetailsPage = ServiceTestUtil.giveContentsPage(
                contentReportsRepository::findByElement, content.getClass());

        List<Long> contentIds = Arrays.asList(
                requestDto.getPostId(), requestDto.getCommentId(), requestDto.getUserId());

        // when
        ContentReportDetailDto<Dto> result = reportsService.listContentReportDetails(requestDto);

        // then
        assertEquals(reportDetailsPage, result.getContentsPage().getContents());
        assertEquals(type.getDetail(), result.getType());

        verifyFindContentById(contentType, contentIds);
        verify(typesService).findByContentType(eq(contentType));
        verify(contentReportsRepository).findByElement(eq(content), any(Pageable.class));

        return result;
    }

    private ContentReportsSaveDto createReportsSaveDto(ContentType contentType) {
        ContentReportsSaveDto requestDto = new ContentReportsSaveDto();
        requestDto.setReasonsId(ID);

        switch (contentType) {
            case POSTS -> requestDto.setPostsId(ID);
            case COMMENTS -> requestDto.setCommentsId(ID);
            case USERS -> requestDto.setUserId(ID);
        }

        return requestDto;
    }

    private ContentReportDetailRequestDto createReportDetailRequestDto(ContentType contentType) {
        Long postId = null;
        Long commentId = null;
        Long userId = null;
        if (POSTS.equals(contentType)) {
            postId = ID;
        } else if (COMMENTS.equals(contentType)) {
            commentId = ID;
        } else {
            userId = ID;
        }

        return ContentReportDetailRequestDto.builder()
                .postId(postId).commentId(commentId).userId(userId).page(1).build();
    }

    private Pair<Principal, User> givePrincipal() {
        return ServiceTestUtil.givePrincipal(principalHelper::getUserFromPrincipal);
    }

    private Reasons giveReason() {
        Reasons reason = mock(Reasons.class);
        when(reason.getDetail()).thenReturn("신고 사유");
        when(reasonsService.findById(anyLong())).thenReturn(reason);

        return reason;
    }

    private Types giveType() {
        Types type = mock(Types.class);
        when(typesService.findByContentType(any(ContentType.class))).thenReturn(type);

        return type;
    }

    private Posts givePost() {
        return ServiceTestUtil.givePost(postsService::findById);
    }

    private Comments giveComment() {
        return ServiceTestUtil.giveComment(commentsService::findById);
    }

    private User giveUser() {
        return ServiceTestUtil.giveUser(userService::findById, Long.class);
    }

    private ContentReports giveReport() {
        ContentReports report = mock(ContentReports.class);
        when(report.getId()).thenReturn(ID);
        when(contentReportsRepository.save(any(ContentReports.class))).thenReturn(report);

        return report;
    }

    private void giveVisitor(ContentType contentType, Element element) {
        doAnswer(invocation -> {
            Visitor visitor = invocation.getArgument(0);
            giveVisit(visitor, contentType, element);
            return null;
        }).when(element).accept(any(Visitor.class));
    }

    private void giveVisit(Visitor visitor, ContentType contentType, Element element) {
        switch (contentType) {
            case POSTS -> visitor.visit((Posts) element);
            case COMMENTS -> visitor.visit((Comments) element);
            case USERS -> visitor.visit((User) element);
        }
    }

    private void giveSummarySaveDto(User reporter, Reasons reason) {
        when(contentReportsRepository.findReportedDateByElement(any())).thenReturn(NOW);
        when(contentReportsRepository.findReporterByElement(any())).thenReturn(reporter);
        when(contentReportsRepository.findReasonByElement(any())).thenReturn(reason);
    }

    private void verifyFindContentById(ContentType contentType, List<Long> contentIds) {
        List<VerificationMode> modes  = ServiceTestUtil.setModes(contentType);

        verify(postsService, modes.get(0)).findById(eq(contentIds.get(0)));
        verify(commentsService, modes.get(1)).findById(eq(contentIds.get(1)));
        verify(userService, modes.get(2)).findById(eq(contentIds.get(2)));
    }

    private void verifyAfterCountByContents(VerificationMode mode, Element content) {
        verify(contentReportsRepository, mode).findReportedDateByElement(eq(content));
        verify(contentReportsRepository, mode).findReporterByElement(eq(content));
        verify(contentReportsRepository, mode).findReasonByElement(eq(content));
        verify(contentReportsRepository, never()).findOtherReasonByElement(eq(content), any(Reasons.class));
        verify(reportSummaryService, mode).saveOrUpdateContentReportSummary(any(ContentReportSummarySaveDto.class));
    }
}
