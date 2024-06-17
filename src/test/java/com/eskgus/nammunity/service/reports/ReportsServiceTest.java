package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.reports.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    public void savePostReportsOnly() {
        Pair<ContentReportsSaveDto, Posts> pair = createRequestDtoAndPost();

        testSaveContentReportsOnly(pair);

        verifyPosts(pair.getSecond());
    }

    @Test
    public void savePostReportsAndSummary() {
        Pair<ContentReportsSaveDto, Posts> pair = createRequestDtoAndPost();

        testSaveContentReportsAndSummary(pair);

        verifyPosts(pair.getSecond());
    }

    @Test
    public void saveCommentReportsOnly() {
        Pair<ContentReportsSaveDto, Comments> pair = createRequestDtoAndComment();

        testSaveContentReportsOnly(pair);

        verifyComments(pair.getSecond());
    }

    @Test
    public void saveCommentReportsAndSummary() {
        Pair<ContentReportsSaveDto, Comments> pair = createRequestDtoAndComment();

        testSaveContentReportsAndSummary(pair);

        verifyComments(pair.getSecond());
    }

    @Test
    public void saveUserReportsOnly() {
        Pair<ContentReportsSaveDto, User> pair = createRequestDtoAndUser();

        testSaveContentReportsOnly(pair);

        verifyUser(pair.getSecond());
    }

    @Test
    public void saveUserReportsAndSummary() {
        Pair<ContentReportsSaveDto, User> pair = createRequestDtoAndUser();

        testSaveContentReportsAndSummary(pair);

        verifyUser(pair.getSecond());
    }

    @Test
    public void saveWithoutPrincipal() {
        // given
        when(principalHelper.getUserFromPrincipal(null, true))
                .thenThrow(IllegalArgumentException.class);

        ContentReportsSaveDto requestDto = mock(ContentReportsSaveDto.class);

        // when/then
        assertThrowsAndVerifyBeforeReason(requestDto, null);
        verify(reasonsService, never()).findById(anyLong());
        verifyAfterReason();
    }

    @Test
    public void saveWithNonExistentReasonId() {
        // given
        Pair<ContentReportsSaveDto, Principal> pair = createRequestDtoAndPrincipal();
        ContentReportsSaveDto requestDto = pair.getFirst();
        Principal principal = pair.getSecond();

        when(reasonsService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrowsAndVerifyReason(requestDto, principal);
        verifyAfterReason();
    }

    @Test
    public void saveWithoutOtherReason() {
        // given
        Pair<ContentReportsSaveDto, Principal> pair = createCommonPairAndReason("기타");
        ContentReportsSaveDto requestDto = pair.getFirst();
        Principal principal = pair.getSecond();

        when(requestDto.getOtherReasons()).thenReturn(null);

        // when/then
        String exceptionMessage = assertThrowsAndVerifyReason(requestDto, principal);
        assertEquals("기타 사유를 입력하세요.", exceptionMessage);
        verifyAfterReason();
    }

    @Test
    public void saveWithNonExistentPostId() {
        // given
        Pair<ContentReportsSaveDto, Principal> pair = createCommonPairAndReason("신고 사유");
        ContentReportsSaveDto requestDto = pair.getFirst();
        Principal principal = pair.getSecond();

        Long postId = 1L;
        when(requestDto.getPostsId()).thenReturn(postId);
        when(postsService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrowsAndVerifyReason(requestDto, principal);
        verify(postsService).findById(eq(postId));
        verifyAfterReason(ContentType.POSTS, never());
    }

    @Test
    public void saveWithNonExistentCommentId() {
        // given
        Pair<ContentReportsSaveDto, Principal> pair = createCommonPairAndReason("신고 사유");
        ContentReportsSaveDto requestDto = pair.getFirst();
        Principal principal = pair.getSecond();

        Long commentId = 1L;
        when(requestDto.getPostsId()).thenReturn(null);
        when(requestDto.getCommentsId()).thenReturn(commentId);
        when(commentsService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrowsAndVerifyReason(requestDto, principal);
        verify(commentsService).findById(eq(commentId));
        verifyAfterReason(ContentType.COMMENTS, never());
    }

    @Test
    public void saveWithNonExistentUserId() {
        // given
        Pair<ContentReportsSaveDto, Principal> pair = createCommonPairAndReason("신고 사유");
        ContentReportsSaveDto requestDto = pair.getFirst();
        Principal principal = pair.getSecond();

        Long userId = 1L;
        when(requestDto.getPostsId()).thenReturn(null);
        when(requestDto.getCommentsId()).thenReturn(null);
        when(requestDto.getUserId()).thenReturn(userId);
        when(userService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrowsAndVerifyReason(requestDto, principal);
        verify(userService).findById(eq(userId));
        verifyAfterReason(ContentType.USERS, never());
    }

    @Test
    public void saveWithoutContentType() {
        // given
        Pair<ContentReportsSaveDto, Principal> pair = createCommonPairAndReason("신고 사유");
        ContentReportsSaveDto requestDto = pair.getFirst();
        Principal principal = pair.getSecond();

        when(requestDto.getPostsId()).thenReturn(null);
        when(requestDto.getCommentsId()).thenReturn(null);
        when(requestDto.getUserId()).thenReturn(null);

        // when/then
        String exceptionMessage = assertThrowsAndVerifyReason(requestDto, principal);
        assertEquals("신고 분류가 선택되지 않았습니다.", exceptionMessage);
        verifyAfterReason();
    }

    @Test
    public void saveWithNonExistentContentType() {
        // given
        Pair<ContentReportsSaveDto, Principal> pair = createCommonPairAndReason("신고 사유");
        ContentReportsSaveDto requestDto = pair.getFirst();
        Principal principal = pair.getSecond();

        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);
        when(requestDto.getPostsId()).thenReturn(1L);
        when(postsService.findById(anyLong())).thenReturn(post);

        when(typesService.findByContentType(any(ContentType.class))).thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrowsAndVerifyReason(requestDto, principal);
        verify(postsService).findById(eq(post.getId()));
        verifyAfterReason(ContentType.POSTS, times(1));
    }

    @Test
    public void listPostReportDetails() {
        ContentReportDetailRequestDto requestDto = createDetailRequestDto(1L, null, null);

        Posts post = mock(Posts.class);
        User user = mock(User.class);
        when(post.getId()).thenReturn(1L);
        when(post.getUser()).thenReturn(user);
        when(post.getModifiedDate()).thenReturn(LocalDateTime.now());
        when(postsService.findById(post.getId())).thenReturn(post);

        testListContentReportDetails(ContentType.POSTS, requestDto, post);

        verify(postsService).findById(eq(post.getId()));
    }

    @Test
    public void listCommentReportDetails() {
        ContentReportDetailRequestDto requestDto = createDetailRequestDto(null, 1L, null);

        Comments comment = mock(Comments.class);
        User user = mock(User.class);
        Posts post = mock(Posts.class);
        when(comment.getId()).thenReturn(1L);
        when(comment.getUser()).thenReturn(user);
        when(comment.getModifiedDate()).thenReturn(LocalDateTime.now());
        when(comment.getPosts()).thenReturn(post);
        when(commentsService.findById(comment.getId())).thenReturn(comment);

        testListContentReportDetails(ContentType.COMMENTS, requestDto, comment);

        verify(commentsService).findById(eq(comment.getId()));
    }

    @Test
    public void listUserReportDetails() {
        ContentReportDetailRequestDto requestDto = createDetailRequestDto(null, null, 1L);

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getCreatedDate()).thenReturn(LocalDateTime.now());
        when(userService.findById(user.getId())).thenReturn(user);

        testListContentReportDetails(ContentType.USERS, requestDto, user);

        verify(userService).findById(eq(user.getId()));
    }

    @Test
    public void listDetailsWithNonExistentPostId() {
        // given
        when(postsService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        ContentReportDetailRequestDto requestDto = createDetailRequestDto(1L, null, null);

        // when/then
        assertThrowsAndVerify(requestDto, ContentType.POSTS, never());
        verify(postsService).findById(eq(requestDto.getPostId()));
    }

    @Test
    public void listDetailsWithNonExistentCommentId() {
        // given
        when(commentsService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        ContentReportDetailRequestDto requestDto = createDetailRequestDto(null, 1L, null);

        // when/then
        assertThrowsAndVerify(requestDto, ContentType.COMMENTS, never());
        verify(commentsService).findById(eq(requestDto.getCommentId()));
    }

    @Test
    public void listDetailsWithNonExistentUserId() {
        // given
        when(userService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        ContentReportDetailRequestDto requestDto = createDetailRequestDto(null, null, 1L);

        // when/then
        assertThrowsAndVerify(requestDto, ContentType.USERS, never());
        verify(userService).findById(eq(requestDto.getUserId()));
    }

    @Test
    public void listDetailsWithNonExistentContentType() {
        // given
        Posts post = mock(Posts.class);
        User user = mock(User.class);
        when(post.getId()).thenReturn(1L);
        when(post.getUser()).thenReturn(user);
        when(post.getModifiedDate()).thenReturn(LocalDateTime.now());
        when(postsService.findById(post.getId())).thenReturn(post);

        when(typesService.findByContentType(any(ContentType.class))).thenThrow(IllegalArgumentException.class);

        ContentReportDetailRequestDto requestDto = createDetailRequestDto(post.getId(), null, null);

        // when/then
        assertThrowsAndVerify(requestDto, ContentType.POSTS, times(1));
        verify(postsService).findById(eq(post.getId()));
    }

    private Pair<ContentReportsSaveDto, Posts> createRequestDtoAndPost() {
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);
        when(postsService.findById(post.getId())).thenReturn(post);

        ContentReportsSaveDto requestDto = mock(ContentReportsSaveDto.class);
        when(requestDto.getPostsId()).thenReturn(1L);

        return Pair.of(requestDto, post);
    }

    private Pair<ContentReportsSaveDto, Comments> createRequestDtoAndComment() {
        Comments comment = mock(Comments.class);
        when(comment.getId()).thenReturn(1L);
        when(commentsService.findById(comment.getId())).thenReturn(comment);

        ContentReportsSaveDto requestDto = mock(ContentReportsSaveDto.class);
        when(requestDto.getPostsId()).thenReturn(null);
        when(requestDto.getCommentsId()).thenReturn(1L);

        return Pair.of(requestDto, comment);
    }

    private Pair<ContentReportsSaveDto, User> createRequestDtoAndUser() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userService.findById(user.getId())).thenReturn(user);

        ContentReportsSaveDto requestDto = mock(ContentReportsSaveDto.class);
        when(requestDto.getPostsId()).thenReturn(null);
        when(requestDto.getCommentsId()).thenReturn(null);
        when(requestDto.getUserId()).thenReturn(1L);

        return Pair.of(requestDto, user);
    }

    private Pair<ContentReportsSaveDto, Principal> createCommonPairAndReason(String reasonDetail) {
        Pair<ContentReportsSaveDto, Principal> pair = createRequestDtoAndPrincipal();
        Reasons reason = mock(Reasons.class);
        when(reason.getDetail()).thenReturn(reasonDetail);
        when(reasonsService.findById(anyLong())).thenReturn(reason);

        return pair;
    }

    private Pair<ContentReportsSaveDto, Principal> createRequestDtoAndPrincipal() {
        Principal principal = mock(Principal.class);
        User reporter = mock(User.class);
        when(principalHelper.getUserFromPrincipal(principal, true)).thenReturn(reporter);

        ContentReportsSaveDto requestDto = mock(ContentReportsSaveDto.class);
        when(requestDto.getReasonsId()).thenReturn(1L);

        return Pair.of(requestDto, principal);
    }

    private <T> void testSaveContentReportsOnly(Pair<ContentReportsSaveDto, T> pair) {
        testSaveContentReports(pair, 1);

        verify(contentReportsRepository, never()).findReportedDateByContents(any());
        verify(contentReportsRepository, never()).findReporterByContents(any());
        verify(contentReportsRepository, never()).findReasonByContents(any());
        verify(contentReportsRepository, never()).findOtherReasonByContents(any(), any(Reasons.class));
        verify(reportSummaryService, never()).saveOrUpdateContentReportSummary(any(ContentReportSummarySaveDto.class));
    }

    private <T> void testSaveContentReportsAndSummary(Pair<ContentReportsSaveDto, T> pair) {
        Reasons reasonSummary = mock(Reasons.class);
        when(reasonSummary.getDetail()).thenReturn("기타");
        when(contentReportsRepository.findReasonByContents(any())).thenReturn(reasonSummary);

        testSaveContentReports(pair, 10);

        T content = pair.getSecond();
        verify(contentReportsRepository).findReportedDateByContents(eq(content));
        verify(contentReportsRepository).findReporterByContents(eq(content));
        verify(contentReportsRepository).findReasonByContents(eq(content));
        verify(contentReportsRepository).findOtherReasonByContents(eq(content), any(Reasons.class));
        verify(reportSummaryService).saveOrUpdateContentReportSummary(any(ContentReportSummarySaveDto.class));
    }

    private <T> void testSaveContentReports(Pair<ContentReportsSaveDto, T> pair, long count) {
        // given
        ContentReportsSaveDto requestDto = pair.getFirst();
        when(requestDto.getReasonsId()).thenReturn(1L);

        Principal principal = mock(Principal.class);
        User reporter = mock(User.class);
        when(principalHelper.getUserFromPrincipal(principal, true)).thenReturn(reporter);

        Reasons reason = mock(Reasons.class);
        when(reason.getDetail()).thenReturn("신고 사유");
        when(reasonsService.findById(anyLong())).thenReturn(reason);

        ContentReports contentReports = mock(ContentReports.class);
        when(contentReports.getId()).thenReturn(1L);
        when(contentReportsRepository.save(any(ContentReports.class))).thenReturn(contentReports);

        when(contentReportsRepository.countByContents(any())).thenReturn(count);

        // when
        Long result = reportsService.saveContentReports(requestDto, principal);

        // then
        assertEquals(contentReports.getId(), result);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(reasonsService).findById(eq(requestDto.getReasonsId()));
        verify(contentReportsRepository).save(any(ContentReports.class));
        verify(contentReportsRepository).countByContents(eq(pair.getSecond()));
    }

    private void verifyPosts(Posts post) {
        verify(postsService).findById(eq(post.getId()));
        verify(typesService).findByContentType(ContentType.POSTS);
    }

    private void verifyComments(Comments comment) {
        verify(commentsService).findById(eq(comment.getId()));
        verify(typesService).findByContentType(ContentType.COMMENTS);
    }

    private void verifyUser(User user) {
        verify(userService).findById(eq(user.getId()));
        verify(typesService).findByContentType(ContentType.USERS);
    }

    private String assertThrowsAndVerifyReason(ContentReportsSaveDto requestDto, Principal principal) {
        String exceptionMessage = assertThrowsAndVerifyBeforeReason(requestDto, principal);
        verify(reasonsService).findById(eq(requestDto.getReasonsId()));

        return exceptionMessage;
    }

    private String assertThrowsAndVerifyBeforeReason(ContentReportsSaveDto requestDto, Principal principal) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reportsService.saveContentReports(requestDto, principal));

        verify(principalHelper).getUserFromPrincipal(principal, true);

        return exception.getMessage();
    }

    private void verifyAfterReason() {
        verifyNoContent();
        verifyNoSaveReportsAndSummary();
    }

    private void verifyAfterReason(ContentType contentType, VerificationMode mode) {
        verifyContent(contentType, mode);
        verifyNoSaveReportsAndSummary();
    }

    private void verifyNoContent() {
        verify(postsService, never()).findById(anyLong());
        verify(commentsService, never()).findById(anyLong());
        verify(userService, never()).findById(anyLong());
        verify(typesService, never()).findByContentType(any(ContentType.class));
    }

    private void verifyContent(ContentType contentType, VerificationMode mode) {
        if (!contentType.equals(ContentType.POSTS)) {
            verify(postsService, never()).findById(anyLong());
        }
        if (!contentType.equals(ContentType.COMMENTS)) {
            verify(commentsService, never()).findById(anyLong());
        }
        if (!contentType.equals(ContentType.USERS)) {
            verify(userService, never()).findById(anyLong());
        }
        verify(typesService, mode).findByContentType(any(ContentType.class));
    }

    private void verifyNoSaveReportsAndSummary() {
        verify(contentReportsRepository, never()).save(any(ContentReports.class));
        verify(contentReportsRepository, never()).countByContents(any());
        verifyNoSaveSummary();
    }

    private void verifyNoSaveSummary() {
        verify(contentReportsRepository, never()).findReportedDateByContents(any());
        verify(contentReportsRepository, never()).findReporterByContents(any());
        verify(contentReportsRepository, never()).findReasonByContents(any());
        verify(contentReportsRepository, never()).findOtherReasonByContents(any(), any(Reasons.class));
        verify(reportSummaryService, never()).saveOrUpdateContentReportSummary(any(ContentReportSummarySaveDto.class));
    }

    private <T> void testListContentReportDetails(ContentType contentType, ContentReportDetailRequestDto requestDto,
                                                  T content) {
        // given
        Types type = mock(Types.class);
        when(type.getDetail()).thenReturn(contentType.getDetailInKor());
        when(typesService.findByContentType(any(ContentType.class))).thenReturn(type);

        Page<ContentReportDetailListDto> contents = new PageImpl<>(Collections.emptyList());
        when(contentReportsRepository.findByContents(any(), any(Pageable.class))).thenReturn(contents);

        // when
        ContentReportDetailDto result = reportsService.listContentReportDetails(requestDto);

        // then
        assertEquals(contents, result.getContentsPage().getContents());

        verify(typesService).findByContentType(eq(contentType));
        verify(contentReportsRepository).findByContents(eq(content), any(Pageable.class));
    }

    private ContentReportDetailRequestDto createDetailRequestDto(Long postId, Long commentId, Long userId) {
        return ContentReportDetailRequestDto.builder()
                .postId(postId).commentId(commentId).userId(userId).page(1).build();
    }

    private void assertThrowsAndVerify(ContentReportDetailRequestDto requestDto,
                                       ContentType contentType, VerificationMode mode) {
        assertThrows(IllegalArgumentException.class, () -> reportsService.listContentReportDetails(requestDto));
        verifyContent(contentType, mode);
    }
}
