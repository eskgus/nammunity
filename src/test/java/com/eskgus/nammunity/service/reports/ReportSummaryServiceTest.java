package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDeleteDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummarySaveDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.eskgus.nammunity.domain.enums.ContentType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportSummaryServiceTest {
    @Mock
    private ContentReportSummaryRepository contentReportSummaryRepository;

    @Mock
    private TypesService typesService;

    @Mock
    private PostsService postsService;

    @Mock
    private CommentsService commentsService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReportSummaryService reportSummaryService;

    @Test
    public void savePostReportSummary() {
        Pair<ContentReportSummarySaveDto, Posts> pair = createRequestDtoAndPost();

        testSaveContentReportSummary(pair);
    }

    @Test
    public void updatePostReportSummary() {
        Pair<ContentReportSummarySaveDto, Posts> pair = createRequestDtoAndPost();

        testUpdateContentReportSummary(pair);
    }

    @Test
    public void saveCommentReportSummary() {
        Pair<ContentReportSummarySaveDto, Comments> pair = createRequestDtoAndComment();

        testSaveContentReportSummary(pair);
    }

    @Test
    public void updateCommentReportSummary() {
        Pair<ContentReportSummarySaveDto, Comments> pair = createRequestDtoAndComment();

        testUpdateContentReportSummary(pair);
    }

    @Test
    public void saveUserReportSummary() {
        Pair<ContentReportSummarySaveDto, User> pair = createRequestDtoAndUser();

        testSaveContentReportSummary(pair);
    }

    @Test
    public void updateUserReportSummary() {
        Pair<ContentReportSummarySaveDto, User> pair = createRequestDtoAndUser();

        testUpdateContentReportSummary(pair);
    }

    @Test
    public void findAllDesc() {
        // given
        Page<ContentReportSummaryDto> reportSummaryPage = new PageImpl<>(Collections.emptyList());
        when(contentReportSummaryRepository.findAllDesc(any(Pageable.class))).thenReturn(reportSummaryPage);

        int page = 1;

        // when
        ContentsPageDto<ContentReportSummaryDto> result = reportSummaryService.findAllDesc(page);

        // then
        assertEquals(reportSummaryPage, result.getContents());

        verify(contentReportSummaryRepository).findAllDesc(any(Pageable.class));
    }

    @Test
    public void findByPostTypes() {
        testFindByTypes(POSTS);
    }

    @Test
    public void findByCommentTypes() {
        testFindByTypes(COMMENTS);
    }

    @Test
    public void findByUserTypes() {
        testFindByTypes(USERS);
    }

    @Test
    public void findByTypesWithNonExistentContentType() {
        // given
        when(typesService.findByContentType(any(ContentType.class))).thenThrow(IllegalArgumentException.class);

        ContentType contentType = POSTS;
        int page = 1;

        // when/then
        assertThrows(IllegalArgumentException.class, () -> reportSummaryService.findByTypes(contentType, page));

        verify(typesService).findByContentType(eq(contentType));
        verify(contentReportSummaryRepository, never()).findByTypes(any(Types.class), any(Pageable.class));
    }

    @Test
    public void deleteSelectedReportSummary() {
        // given
        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);
        when(postsService.findById(post.getId())).thenReturn(post);

        Comments comment = mock(Comments.class);
        when(comment.getId()).thenReturn(1L);
        when(commentsService.findById(comment.getId())).thenReturn(comment);

        ContentReportSummaryDeleteDto deleteDto
                = createDeleteDto(Collections.singletonList(post.getId()),
                    Collections.singletonList(comment.getId()),
                    Collections.emptyList());

        // when
        reportSummaryService.deleteSelectedReportSummary(deleteDto);

        // then
        verify(postsService).findById(eq(post.getId()));
        verify(commentsService).findById(eq(comment.getId()));
        verify(userService, never()).findById(anyLong());
        verify(contentReportSummaryRepository, times(2)).deleteByContents(any());
    }

    @Test
    public void deleteSelectedReportSummaryWithEmptyContentsId() {
        // given
        ContentReportSummaryDeleteDto deleteDto
                = createDeleteDto(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        // when/then
        String exceptionMessage = assertThrowsAndVerify(deleteDto, null);
        assertEquals("삭제할 항목을 선택하세요.", exceptionMessage);
    }

    @Test
    public void deleteSelectedReportSummaryWithNonExistentPostId() {
        // given
        when(postsService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        Long postId = 1L;
        ContentReportSummaryDeleteDto deleteDto
                = createDeleteDto(Collections.singletonList(postId), Collections.emptyList(), Collections.emptyList());

        // when/then
        assertThrowsAndVerify(deleteDto, POSTS);
        verify(postsService).findById(eq(postId));
    }

    @Test
    public void deleteSelectedReportSummaryWithNonExistentCommentId() {
        // given
        when(commentsService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        Long commentId = 1L;
        ContentReportSummaryDeleteDto deleteDto
                = createDeleteDto(Collections.emptyList(), Collections.singletonList(commentId), Collections.emptyList());

        // when/then
        assertThrowsAndVerify(deleteDto, COMMENTS);
        verify(commentsService).findById(eq(commentId));
    }

    @Test
    public void deleteSelectedReportSummaryWithNonExistentUserId() {
        // given
        when(userService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        Long userId = 1L;
        ContentReportSummaryDeleteDto deleteDto
                = createDeleteDto(Collections.emptyList(), Collections.emptyList(), Collections.singletonList(userId));

        // when/then
        assertThrowsAndVerify(deleteDto, USERS);
        verify(userService).findById(eq(userId));
    }

    private Pair<ContentReportSummarySaveDto, Posts> createRequestDtoAndPost() {
        Posts post = mock(Posts.class);
        ContentReportSummarySaveDto requestDto = createContentReportSummarySaveDto(post, null, null);
        return Pair.of(requestDto, post);
    }

    private Pair<ContentReportSummarySaveDto, Comments> createRequestDtoAndComment() {
        Comments comment = mock(Comments.class);
        ContentReportSummarySaveDto requestDto = createContentReportSummarySaveDto(null, comment, null);
        return Pair.of(requestDto, comment);
    }

    private Pair<ContentReportSummarySaveDto, User> createRequestDtoAndUser() {
        User user = mock(User.class);
        ContentReportSummarySaveDto requestDto = createContentReportSummarySaveDto(null, null, user);
        return Pair.of(requestDto, user);
    }

    private ContentReportSummarySaveDto createContentReportSummarySaveDto(Posts post, Comments comment, User user) {
        ContentType contentType = Optional.ofNullable(post).map(p -> POSTS)
                .orElse(Optional.ofNullable(comment).map(c -> COMMENTS)
                        .orElse(USERS));

        Types type = mock(Types.class);
        when(type.getDetail()).thenReturn(contentType.getDetail());

        LocalDateTime reportedDate = LocalDateTime.now();
        User reporter = mock(User.class);
        Reasons reason = mock(Reasons.class);
        return ContentReportSummarySaveDto.builder()
                .posts(post).comments(comment).user(user)
                .types(type).reportedDate(reportedDate).reporter(reporter).reasons(reason).build();
    }

    private <T> void testSaveContentReportSummary(Pair<ContentReportSummarySaveDto, T> pair) {
        ContentReportSummary reportSummary = testSaveOrUpdateContentReportSummary(pair, false);

        verify(contentReportSummaryRepository).save(any(ContentReportSummary.class));
        verify(contentReportSummaryRepository, never()).findByContents(any());
        verify(reportSummary, never()).update(any(LocalDateTime.class), any(User.class), any(Reasons.class), isNull());
    }

    private <T> void testUpdateContentReportSummary(Pair<ContentReportSummarySaveDto, T> pair) {
        ContentReportSummary reportSummary = testSaveOrUpdateContentReportSummary(pair, true);

        verify(contentReportSummaryRepository, never()).save(any(ContentReportSummary.class));
        verify(contentReportSummaryRepository).findByContents(eq(pair.getSecond()));

        ContentReportSummarySaveDto requestDto = pair.getFirst();
        verify(reportSummary)
                .update(eq(requestDto.getReportedDate()), eq(requestDto.getReporter()),
                        eq(requestDto.getReasons()), isNull());
    }

    private <T> ContentReportSummary testSaveOrUpdateContentReportSummary(Pair<ContentReportSummarySaveDto, T> pair,
                                                                          boolean doContentsExist) {
        // given
        when(contentReportSummaryRepository.existsByContents(any())).thenReturn(doContentsExist);

        ContentReportSummary reportSummary = mock(ContentReportSummary.class);
        when(reportSummary.getId()).thenReturn(1L);

        if (doContentsExist) {
            when(contentReportSummaryRepository.findByContents(any())).thenReturn(reportSummary);
        } else {
            when(contentReportSummaryRepository.save(any(ContentReportSummary.class))).thenReturn(reportSummary);
        }

        // when
        Long result = reportSummaryService.saveOrUpdateContentReportSummary(pair.getFirst());

        // then
        assertEquals(reportSummary.getId(), result);

        verify(contentReportSummaryRepository).existsByContents(eq(pair.getSecond()));

        return reportSummary;
    }

    private void testFindByTypes(ContentType contentType) {
        // given
        Types type = mock(Types.class);
        when(typesService.findByContentType(any(ContentType.class))).thenReturn(type);

        Page<ContentReportSummaryDto> reportSummaryPage = new PageImpl<>(Collections.emptyList());
        when(contentReportSummaryRepository.findByTypes(any(Types.class), any(Pageable.class)))
                .thenReturn(reportSummaryPage);

        int page = 1;

        // when
        ContentsPageDto<ContentReportSummaryDto> result = reportSummaryService.findByTypes(contentType, page);

        // then
        assertEquals(reportSummaryPage, result.getContents());

        verify(typesService).findByContentType(eq(contentType));
        verify(contentReportSummaryRepository).findByTypes(eq(type), any(Pageable.class));
    }

    private ContentReportSummaryDeleteDto createDeleteDto(List<Long> postsId, List<Long> commentsId, List<Long> userId) {
        return ContentReportSummaryDeleteDto.builder()
                .postsId(postsId).commentsId(commentsId).userId(userId).build();
    }

    private String assertThrowsAndVerify(ContentReportSummaryDeleteDto deleteDto, ContentType contentType) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reportSummaryService.deleteSelectedReportSummary(deleteDto));

        verifyDelete(contentType);

        return exception.getMessage();
    }

    private void verifyDelete(ContentType contentType) {
        if (!POSTS.equals(contentType)) {
            verify(postsService, never()).findById(anyLong());
        }
        if (!COMMENTS.equals(contentType)) {
            verify(commentsService, never()).findById(anyLong());
        }
        if (!USERS.equals(contentType)) {
            verify(userService, never()).findById(anyLong());
        }
        verify(contentReportSummaryRepository, never()).deleteByContents(any());
    }
}
