package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.converter.CommentsConverterForTest;
import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.converter.PostsConverterForTest;
import com.eskgus.nammunity.converter.UserConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDeleteDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummarySaveDto;
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

import java.time.LocalDateTime;
import java.util.*;

import static com.eskgus.nammunity.domain.enums.ContentType.*;
import static com.eskgus.nammunity.domain.enums.Fields.OTHER;
import static com.eskgus.nammunity.util.ServiceTestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static final Long ID = 1L;

    @Test
    public void savePostReportSummary() {
        Posts post = mock(Posts.class);
        testSaveContentReportSummary(POSTS, post);
    }

    @Test
    public void saveCommentReportSummary() {
        Comments comment = mock(Comments.class);
        testSaveContentReportSummary(COMMENTS, comment);
    }

    @Test
    public void saveUserReportSummary() {
        User user = mock(User.class);
        testSaveContentReportSummary(USERS, user);
    }

    @Test
    public void updatePostReportSummary() {
        Posts post = mock(Posts.class);
        testUpdateContentReportSummary(POSTS, post);
    }

    @Test
    public void updateCommentReportSummary() {
        Comments comment = mock(Comments.class);
        testUpdateContentReportSummary(COMMENTS, comment);
    }

    @Test
    public void updateUserReportSummary() {
        User user = mock(User.class);
        testUpdateContentReportSummary(USERS, user);
    }

    @Test
    public void findAllSummariesDesc() {
        // given
        int page = 1;

        Page<ContentReportSummaryDto> summariesPage = new PageImpl<>(Collections.emptyList());
        when(contentReportSummaryRepository.findAllDesc(any(Pageable.class))).thenReturn(summariesPage);

        // when
        ContentsPageDto<ContentReportSummaryDto> result = reportSummaryService.findAllDesc(page);

        // then
        assertEquals(summariesPage, result.getContents());

        verify(contentReportSummaryRepository).findAllDesc(any(Pageable.class));
    }

    @Test
    public void findSummaryByTypes() {
        // given
        ContentType contentType = POSTS;
        int page = 1;

        Types type = mock(Types.class);
        when(typesService.findByContentType(any(ContentType.class))).thenReturn(type);

        Page<ContentReportSummaryDto> summariesPage = new PageImpl<>(Collections.emptyList());
        when(contentReportSummaryRepository.findByTypes(any(Types.class), any(Pageable.class))).thenReturn(summariesPage);

        // when
        ContentsPageDto<ContentReportSummaryDto> result = reportSummaryService.findByTypes(contentType, page);

        // then
        assertEquals(summariesPage, result.getContents());

        verify(typesService).findByContentType(eq(contentType));
        verify(contentReportSummaryRepository).findByTypes(eq(type), any(Pageable.class));
    }

    @Test
    public void findSummaryByUser() {
        // given
        User user = mock(User.class);

        ContentReportSummary summary = mock(ContentReportSummary.class);
        when(contentReportSummaryRepository.findByUser(any(User.class))).thenReturn(Optional.of(summary));

        // when
        ContentReportSummary result = reportSummaryService.findByUser(user);

        // then
        assertEquals(summary, result);

        verify(contentReportSummaryRepository).findByUser(eq(user));
    }

    @Test
    public void deleteSelectedReportSummaries() {
        // given
        Pair<List<Posts>, List<Long>> postPair = givePosts();
        List<Posts> posts = postPair.getFirst();
        List<Long> postIds = postPair.getSecond();

        Pair<List<Comments>, List<Long>> commentPair = giveComments();
        List<Comments> comments = commentPair.getFirst();
        List<Long> commentIds = commentPair.getSecond();

        Pair<List<User>, List<Long>> userPair = giveUsers();
        List<User> users = userPair.getFirst();
        List<Long> userIds = userPair.getSecond();

        ContentReportSummaryDeleteDto requestDto = ContentReportSummaryDeleteDto.builder()
                .postsId(postIds).commentsId(commentIds).userId(userIds).build();

        doNothing().when(contentReportSummaryRepository).deleteByContents(any());

        // when
        reportSummaryService.deleteSelectedReportSummaries(requestDto);

        // then
        verifyFindContentById(postIds.size(), commentIds.size(), userIds.size());
        verifyDeleteByContents(posts.size(), comments.size(), users.size());
    }

    private <Entity> ContentReportSummarySaveDto createSummarySaveDto(ContentType contentType, Entity content) {
        Posts post = null;
        Comments comment = null;
        User user = null;
        switch (contentType) {
            case POSTS -> post = (Posts) content;
            case COMMENTS -> comment = (Comments) content;
            case USERS -> user = (User) content;
        }

        Types type = mock(Types.class);
        when(type.getDetail()).thenReturn(contentType.getDetail());

        User reporter = mock(User.class);

        Reasons reason = mock(Reasons.class);

        String otherReason = OTHER.getKey();

        return ContentReportSummarySaveDto.builder()
                .posts(post).comments(comment).user(user)
                .types(type).reportedDate(LocalDateTime.now()).reporter(reporter)
                .reasons(reason).otherReasons(otherReason).build();
    }

    private ContentReportSummary giveSummary() {
        ContentReportSummary summary = mock(ContentReportSummary.class);
        when(summary.getId()).thenReturn(ID);

        return summary;
    }

    private Pair<List<Posts>, List<Long>> givePosts() {
        List<Posts> posts = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Posts post = givePost(ID + i, postsService::findById);
            posts.add(post);
        }

        List<Long> postIds = createContentIds(posts, new PostsConverterForTest());
        return Pair.of(posts, postIds);
    }

    private Pair<List<Comments>, List<Long>> giveComments() {
        List<Comments> comments = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Comments comment = giveComment(ID + i, commentsService::findById);
            comments.add(comment);
        }

        List<Long> commentIds = createContentIds(comments, new CommentsConverterForTest<>());
        return Pair.of(comments, commentIds);
    }

    private Pair<List<User>, List<Long>> giveUsers() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            User user = giveUser(ID + i, userService::findById);
            users.add(user);
        }

        List<Long> userIds = createContentIds(users, new UserConverterForTest());
        return Pair.of(users, userIds);
    }

    private <Entity> List<Long> createContentIds(List<Entity> contents,
                                                 EntityConverterForTest<?, Entity> entityConverter) {
        return ServiceTestUtil.createContentIds(contents, entityConverter);
    }

    private <Entity> void testSaveContentReportSummary(ContentType contentType, Entity content) {
        // given
        ContentReportSummary summary = giveSummary();
        when(contentReportSummaryRepository.save(any(ContentReportSummary.class))).thenReturn(summary);

        // when
        ContentReportSummarySaveDto requestDto = testSaveOrUpdateContentReportSummary(
                contentType, content, false, summary.getId());

        // then
        Pair<VerificationMode, VerificationMode> modePair = Pair.of(times(1), never());
        Pair<ContentReportSummary, ContentReportSummarySaveDto> summaryPair = Pair.of(summary, requestDto);

        verifySaveOrUpdateContentReportSummary(modePair, content, summaryPair);
    }

    private <Entity> void testUpdateContentReportSummary(ContentType contentType, Entity content) {
        // given
        ContentReportSummary summary = giveSummary();
        when(contentReportSummaryRepository.findByContents(any())).thenReturn(summary);

        // when
        ContentReportSummarySaveDto requestDto = testSaveOrUpdateContentReportSummary(
                contentType, content, true, summary.getId());

        // then
        Pair<VerificationMode, VerificationMode> modePair = Pair.of(never(), times(1));
        Pair<ContentReportSummary, ContentReportSummarySaveDto> summaryPair = Pair.of(summary, requestDto);

        verifySaveOrUpdateContentReportSummary(modePair, content, summaryPair);
    }

    private <Entity> ContentReportSummarySaveDto testSaveOrUpdateContentReportSummary(ContentType contentType,
                                                                                      Entity content,
                                                                                      boolean doesSummaryExist,
                                                                                      Long summaryId) {
        // given
        ContentReportSummarySaveDto requestDto = createSummarySaveDto(contentType, content);

        when(contentReportSummaryRepository.existsByContents(any())).thenReturn(doesSummaryExist);

        // when
        Long result = reportSummaryService.saveOrUpdateContentReportSummary(requestDto);

        // then
        assertEquals(summaryId, result);

        verify(contentReportSummaryRepository).existsByContents(eq(content));

        return requestDto;
    }

    private <Entity> void verifySaveOrUpdateContentReportSummary(Pair<VerificationMode, VerificationMode> modePair,
                                                                 Entity content,
                                                                 Pair<ContentReportSummary, ContentReportSummarySaveDto> summaryPair) {
        VerificationMode saveMode = modePair.getFirst();
        VerificationMode updateMode = modePair.getSecond();

        ContentReportSummary summary = summaryPair.getFirst();
        ContentReportSummarySaveDto requestDto = summaryPair.getSecond();

        verify(contentReportSummaryRepository, saveMode).save(any(ContentReportSummary.class));
        verify(contentReportSummaryRepository, updateMode).findByContents(eq(content));
        verify(summary, updateMode).update(eq(requestDto.getReportedDate()), eq(requestDto.getReporter()),
                eq(requestDto.getReasons()), eq(requestDto.getOtherReasons()));
    }

    private void verifyFindContentById(int postIdsSize, int commentIdsSize, int userIdsSize) {
        verify(postsService, times(postIdsSize)).findById(anyLong());
        verify(commentsService, times(commentIdsSize)).findById(anyLong());
        verify(userService, times(userIdsSize)).findById(anyLong());
    }

    private void verifyDeleteByContents(int postsSize, int commentsSize, int usersSize) {
        verify(contentReportSummaryRepository, times(postsSize)).deleteByContents(any(Posts.class));
        verify(contentReportSummaryRepository, times(commentsSize)).deleteByContents(any(Comments.class));
        verify(contentReportSummaryRepository, times(usersSize)).deleteByContents(any(User.class));
    }
}
