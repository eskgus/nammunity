package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.ContentReports;
import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailRequestDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummarySaveDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportsSaveDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.data.util.Pair;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.ContentType.*;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.OTHER;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportsServiceExceptionTest {
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

    @Test
    public void saveReportsWithAnonymousUser() {
        testSaveReportsThrowsPrincipalException(null, UNAUTHORIZED);
    }

    @Test
    public void saveReportsWithNonExistentUsername() {
        Principal principal = mock(Principal.class);
        testSaveReportsThrowsPrincipalException(principal, USERNAME_NOT_FOUND);
    }

    @Test
    public void saveReportsWithNonExistentReason() {
        ExceptionMessages exceptionMessage = REASON_NOT_FOUND;
        throwIllegalArgumentException(reasonsService::findById, exceptionMessage);

        testSaveReportsThrowsReasonException(exceptionMessage);
    }

    @Test
    public void saveReportsWithEmptyOtherReason() {
        Reasons reason = giveReason();
        when(reason.getDetail()).thenReturn(OTHER.getKey());

        testSaveReportsThrowsReasonException(EMPTY_OTHER_REASON);
    }

    @Test
    public void saveReportsWithNonExistentPost() {
        ContentType contentType = POSTS;

        ContentReportsSaveDto requestDto = createContentReportsSaveDto(contentType);
        requestDto.setPostsId(ID);

        Pair<ExceptionMessages, ContentType> pair = Pair.of(POST_NOT_FOUND, contentType);
        testSaveReportsThrowsContentNotFoundException(postsService::findById, pair, requestDto);
    }

    @Test
    public void saveReportsWithNonExistentComment() {
        ContentType contentType = COMMENTS;

        ContentReportsSaveDto requestDto = createContentReportsSaveDto(contentType);
        requestDto.setCommentsId(ID);

        Pair<ExceptionMessages, ContentType> pair = Pair.of(COMMENT_NOT_FOUND, contentType);
        testSaveReportsThrowsContentNotFoundException(commentsService::findById, pair, requestDto);
    }

    @Test
    public void saveReportsWithNonExistentUser() {
        ContentType contentType = USERS;

        ContentReportsSaveDto requestDto = createContentReportsSaveDto(contentType);
        requestDto.setUserId(ID);

        Pair<ExceptionMessages, ContentType> pair = Pair.of(USER_NOT_FOUND, contentType);
        testSaveReportsThrowsContentNotFoundException(userService::findById, pair, requestDto);
    }

    @Test
    public void saveReportsWithEmptyType() {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(null);

        Principal principal = givePrincipal();

        giveReason();

        // when/then
        List<Long> contentIds = testSaveReportsException(requestDto, principal, EMPTY_TYPE);
        verifyFindContents(null, contentIds, never());
    }

    @Test
    public void saveReportsWithNonExistentType() {
        // given
        ContentType contentType = POSTS;

        ContentReportsSaveDto requestDto = createContentReportsSaveDto(contentType);

        Principal principal = givePrincipal();

        giveReason();

        givePost();

        ExceptionMessages exceptionMessage = TYPE_NOT_FOUND;
        throwIllegalArgumentException(typesService::findByContentType, exceptionMessage);

        // when/then
        List<Long> contentIds = testSaveReportsException(requestDto, principal, exceptionMessage);
        verifyFindContents(contentType, contentIds, times(1));
    }

    @Test
    public void listReportDetailsWithNonExistentPost() {
        testListReportDetailsThrowsNotFoundContentException(postsService::findById, POSTS, POST_NOT_FOUND);
    }

    @Test
    public void listReportDetailsWithNonExistentComment() {
        testListReportDetailsThrowsNotFoundContentException(commentsService::findById, COMMENTS, COMMENT_NOT_FOUND);
    }

    @Test
    public void listReportDetailsWithNonExistentUser() {
        testListReportDetailsThrowsNotFoundContentException(userService::findById, USERS, USER_NOT_FOUND);
    }

    @Test
    public void listReportDetailsWithNonExistentType() {
        // given
        ContentType contentType = POSTS;
        ContentReportDetailRequestDto requestDto = createReportDetailRequestDto(contentType);

        Posts post = givePost();
        User user = mock(User.class);
        when(post.getUser()).thenReturn(user);
        when(post.getModifiedDate()).thenReturn(LocalDateTime.now());

        ExceptionMessages exceptionMessage = TYPE_NOT_FOUND;
        throwIllegalArgumentException(typesService::findByContentType, exceptionMessage);

        List<Long> contentIds = Arrays.asList(
                requestDto.getPostId(), requestDto.getCommentId(), requestDto.getUserId());

        // when/then
        assertIllegalArgumentException(() -> reportsService.listContentReportDetails(requestDto), exceptionMessage);

        verifyFindContents(contentType, contentIds, times(1));
    }

    private ContentReportsSaveDto createContentReportsSaveDto(ContentType contentType) {
        ContentReportsSaveDto requestDto = new ContentReportsSaveDto();
        requestDto.setReasonsId(ID);

        if (contentType != null) {
            switch (contentType) {
                case POSTS -> requestDto.setPostsId(ID);
                case COMMENTS -> requestDto.setCommentsId(ID);
                case USERS -> requestDto.setUserId(ID);
            }
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

    private Principal givePrincipal() {
        return ServiceTestUtil.givePrincipal(principalHelper::getUserFromPrincipal).getFirst();
    }

    private Reasons giveReason() {
        Reasons reason = mock(Reasons.class);
        when(reasonsService.findById(anyLong())).thenReturn(reason);

        return reason;
    }

    private Posts givePost() {
        return ServiceTestUtil.givePost(postsService::findById);
    }

    private <Entity, ParamType> void throwIllegalArgumentException(Function<ParamType, Entity> finder,
                                                                   ExceptionMessages exceptionMessage) {
        ServiceTestUtil.throwIllegalArgumentException(finder, exceptionMessage);
    }

    private void testSaveReportsThrowsPrincipalException(Principal principal, ExceptionMessages exceptionMessage) {
        // given
        ContentReportsSaveDto requestDto = mock(ContentReportsSaveDto.class);

        ServiceTestUtil.throwIllegalArgumentException(
                principalHelper::getUserFromPrincipal, principal, true, exceptionMessage);

        // when/then
        List<Long> contentIds = testSaveReportsException(requestDto, principal, exceptionMessage);

        verify(reasonsService, never()).findById(anyLong());
        verifyFindContents(null, contentIds, never());
    }

    private void testSaveReportsThrowsReasonException(ExceptionMessages exceptionMessage) {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(null);

        Principal principal = givePrincipal();

        // when/then
        List<Long> contentIds = testSaveReportsException(requestDto, principal, exceptionMessage);

        verify(reasonsService).findById(eq(requestDto.getReasonsId()));
        verifyFindContents(null, contentIds, never());
    }

    private <Entity> void testSaveReportsThrowsContentNotFoundException(Function<Long, Entity> finder,
                                                                        Pair<ExceptionMessages, ContentType> pair,
                                                                        ContentReportsSaveDto requestDto) {
        // given
        Principal principal = givePrincipal();

        giveReason();

        ExceptionMessages exceptionMessage = pair.getFirst();
        ContentType contentType = pair.getSecond();

        throwIllegalArgumentException(finder, exceptionMessage);

        // when/then
        List<Long> contentIds = testSaveReportsException(requestDto, principal, exceptionMessage);

        verify(reasonsService).findById(eq(requestDto.getReasonsId()));
        verifyFindContents(contentType, contentIds, never());
    }

    private List<Long> testSaveReportsException(ContentReportsSaveDto requestDto, Principal principal,
                                          ExceptionMessages exceptionMessage) {
        assertIllegalArgumentException(() -> reportsService.saveContentReports(requestDto, principal), exceptionMessage);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(contentReportsRepository, never()).save(any(ContentReports.class));
        verify(contentReportsRepository, never()).countByElement(any());
        verify(contentReportsRepository, never()).findReportedDateByElement(any());
        verify(contentReportsRepository, never()).findReporterByElement(any());
        verify(contentReportsRepository, never()).findReasonByElement(any());
        verify(contentReportsRepository, never()).findOtherReasonByElement(any(), any(Reasons.class));
        verify(reportSummaryService, never()).saveOrUpdateContentReportSummary(any(ContentReportSummarySaveDto.class));

        return Arrays.asList(requestDto.getPostsId(), requestDto.getCommentsId(), requestDto.getUserId());
    }

    private <Entity> void testListReportDetailsThrowsNotFoundContentException(Function<Long, Entity> finder,
                                                                              ContentType contentType,
                                                                              ExceptionMessages exceptionMessage) {
        // given
        ContentReportDetailRequestDto requestDto = createReportDetailRequestDto(contentType);

        throwIllegalArgumentException(finder, exceptionMessage);

        List<Long> contentIds = Arrays.asList(
                requestDto.getPostId(), requestDto.getCommentId(), requestDto.getUserId());

        // when/then
        assertIllegalArgumentException(() -> reportsService.listContentReportDetails(requestDto), exceptionMessage);

        verifyFindContents(contentType, contentIds, never());
    }

    private void assertIllegalArgumentException(Executable executable, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.assertIllegalArgumentException(executable, exceptionMessage);
    }

    private void verifyFindContents(ContentType contentType, List<Long> contentIds, VerificationMode typeMode) {
        List<VerificationMode> modes = ServiceTestUtil.setModes(contentType);

        verify(postsService, modes.get(0)).findById(eq(contentIds.get(0)));
        verify(commentsService, modes.get(1)).findById(eq(contentIds.get(1)));
        verify(userService, modes.get(2)).findById(eq(contentIds.get(2)));
        verify(typesService, typeMode).findByContentType(eq(contentType));
    }
}
