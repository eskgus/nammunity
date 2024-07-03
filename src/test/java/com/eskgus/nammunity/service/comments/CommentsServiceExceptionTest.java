package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentsServiceExceptionTest {
    @Mock
    private CommentsRepository commentsRepository;

    @Mock
    private PostsService postsService;

    @Mock
    private PrincipalHelper principalHelper;

    @InjectMocks
    private CommentsService commentsService;

    private static final Long ID = 1L;
    private static final String CONTENT = Fields.CONTENT.getKey();

    @Test
    public void saveCommentsWithAnonymousUser() {
        // given
        ExceptionMessages exceptionMessage = UNAUTHORIZED;

        CommentsSaveDto requestDto = givePrincipalHelperThrowException(null, exceptionMessage);

        // when/then
        testSaveCommentsException(requestDto, null, exceptionMessage, never());
    }

    @Test
    public void saveCommentsWithNonExistentUsername() {
        // given
        ExceptionMessages exceptionMessage = USERNAME_NOT_FOUND;

        Principal principal = mock(Principal.class);

        CommentsSaveDto requestDto = givePrincipalHelperThrowException(principal, exceptionMessage);

        // when/then
        testSaveCommentsException(requestDto, principal, exceptionMessage, never());
    }

    @Test
    public void saveCommentsWithNonExistentPost() {
        // given
        CommentsSaveDto requestDto = createCommentsSaveDto(ID);

        Principal principal = ServiceTestUtil.givePrincipal(principalHelper::getUserFromPrincipal).getFirst();

        ExceptionMessages exceptionMessage = POST_NOT_FOUND;
        throwIllegalArgumentException(postsService::findById, exceptionMessage);

        // when/then
        testSaveCommentsException(requestDto, principal, exceptionMessage, times(1));
    }

    @Test
    public void updateCommentsWithNonExistentComment() {
        // given
        Comments comment = ServiceTestUtil.giveComment(ID);

        ExceptionMessages exceptionMessage = COMMENT_NOT_FOUND;
        throwIllegalArgumentException(commentsRepository::findById, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> commentsService.update(comment.getId(), CONTENT), exceptionMessage);

        verify(commentsRepository).findById(eq(comment.getId()));
        verify(comment, never()).update(eq(CONTENT));
    }

    @Test
    public void deleteSelectedCommentsWithEmptyCommentIds() {
        // given
        List<Long> commentIds = Collections.emptyList();

        // when/then
        testDeleteSelectedCommentsException(commentIds, EMPTY_CONTENT_IDS, never());
    }

    @Test
    public void deleteSelectedCommentsWithNonExistentComment() {
        // given
        List<Long> commentIds = Arrays.asList(ID, ID + 1, ID + 2);

        ExceptionMessages exceptionMessage = COMMENT_NOT_FOUND;
        throwIllegalArgumentException(commentsRepository::findById, exceptionMessage);

        // when/then
        testDeleteSelectedCommentsException(commentIds, exceptionMessage, times(1));
    }

    @Test
    public void deleteCommentsWithNonExistentComment() {
        // given
        ExceptionMessages exceptionMessage = COMMENT_NOT_FOUND;
        throwIllegalArgumentException(commentsRepository::findById, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> commentsService.delete(ID), exceptionMessage);

        verify(commentsRepository).findById(eq(ID));
        verify(commentsRepository, never()).delete(any(Comments.class));
    }

    @Test
    public void findCommentsByIdWithNonExistentComment() {
        // given
        // when/then
        assertIllegalArgumentException(() -> commentsService.findById(ID), COMMENT_NOT_FOUND);

        verify(commentsRepository).findById(eq(ID));
    }

    private CommentsSaveDto givePrincipalHelperThrowException(Principal principal, ExceptionMessages exceptionMessage) {
        Posts post = ServiceTestUtil.givePost(ID);

        CommentsSaveDto requestDto = createCommentsSaveDto(post.getId());

        ServiceTestUtil.throwIllegalArgumentException(
                principalHelper::getUserFromPrincipal, principal, true, exceptionMessage);

        return requestDto;
    }

    private CommentsSaveDto createCommentsSaveDto(Long postId) {
        return new CommentsSaveDto(CONTENT, postId);
    }

    private <Entity> void throwIllegalArgumentException(Function<Long, Entity> finder,
                                                        ExceptionMessages exceptionMessage) {
        ServiceTestUtil.throwIllegalArgumentException(finder, exceptionMessage);
    }

    private void testSaveCommentsException(CommentsSaveDto requestDto, Principal principal,
                                             ExceptionMessages exceptionMessage, VerificationMode mode) {
        assertIllegalArgumentException(() -> commentsService.save(requestDto, principal), exceptionMessage);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(postsService, mode).findById(eq(requestDto.getPostsId()));
        verify(commentsRepository, never()).save(any(Comments.class));
    }

    private void testDeleteSelectedCommentsException(List<Long> commentIds,
                                                     ExceptionMessages exceptionMessage, VerificationMode mode) {
        assertIllegalArgumentException(() -> commentsService.deleteSelectedComments(commentIds), exceptionMessage);

        verify(commentsRepository, mode).findById(anyLong());
        verify(commentsRepository, never()).delete(any(Comments.class));
    }

    private void assertIllegalArgumentException(Executable executable, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.assertIllegalArgumentException(executable, exceptionMessage);
    }
}
