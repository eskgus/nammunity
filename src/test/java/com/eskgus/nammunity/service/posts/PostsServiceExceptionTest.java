package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
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

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.CONTENT;
import static com.eskgus.nammunity.domain.enums.Fields.TITLE;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostsServiceExceptionTest {
    @Mock
    private PostsRepository postsRepository;

    @Mock
    private PrincipalHelper principalHelper;

    @InjectMocks
    private PostsService postsService;

    private static final Long ID = 1L;

    @Test
    public void savePostsWithAnonymousUser() {
        testSavePostsException(null, UNAUTHORIZED);
    }

    @Test
    public void savePostsWithNonExistentUsername() {
        Principal principal = mock(Principal.class);
        testSavePostsException(principal, USERNAME_NOT_FOUND);
    }

    @Test
    public void updatePostsWithNonExistentPost() {
        // given
        Posts post = ServiceTestUtil.givePost(ID);

        PostsUpdateDto requestDto = PostsUpdateDto.builder().title(TITLE.getKey()).content(CONTENT.getKey()).build();

        ExceptionMessages exceptionMessage = throwIllegalArgumentException();

        // when/then
        assertIllegalArgumentException(() -> postsService.update(post.getId(), requestDto), exceptionMessage);

        verify(postsRepository).findById(eq(post.getId()));
        verify(post, never()).update(eq(requestDto.getTitle()), eq(requestDto.getContent()));
    }

    @Test
    public void deleteSelectedPostsWithEmptyPostIds() {
        // given
        List<Long> postIds = Collections.emptyList();

        // when/then
        testDeleteSelectedPostsException(postIds, EMPTY_CONTENTS, never());
    }

    @Test
    public void deleteSelectedPostsWithNonExistentPost() {
        // given
        List<Long> postIds = Arrays.asList(ID, ID + 1, ID + 2);

        ExceptionMessages exceptionMessage = throwIllegalArgumentException();

        // when/then
        testDeleteSelectedPostsException(postIds, exceptionMessage, times(1));
    }

    @Test
    public void deletePostsWithNonExistentPost() {
        // given
        ExceptionMessages exceptionMessage = throwIllegalArgumentException();

        // when/then
        assertIllegalArgumentException(() -> postsService.delete(ID), exceptionMessage);

        verify(postsRepository).findById(eq(ID));
        verify(postsRepository, never()).delete(any(Posts.class));
    }

    @Test
    public void findPostsByIdWithNonExistentPost() {
        // given
        // when/then
        assertIllegalArgumentException(() -> postsService.findById(ID), POST_NOT_FOUND);

        verify(postsRepository).findById(eq(ID));
    }

    private PostsSaveDto createPostsSaveDto() {
        return PostsSaveDto.builder().title(TITLE.getKey()).content(CONTENT.getKey()).build();
    }

    private ExceptionMessages throwIllegalArgumentException() {
        ExceptionMessages exceptionMessage = POST_NOT_FOUND;
        ServiceTestUtil.throwIllegalArgumentException(postsRepository::findById, exceptionMessage);

        return exceptionMessage;
    }

    private void testSavePostsException(Principal principal, ExceptionMessages exceptionMessage) {
        // given
        PostsSaveDto requestDto = createPostsSaveDto();

        ServiceTestUtil.throwIllegalArgumentException(
                principalHelper::getUserFromPrincipal, principal, true, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> postsService.save(requestDto, principal), exceptionMessage);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(postsRepository, never()).save(any(Posts.class));
    }

    private void testDeleteSelectedPostsException(List<Long> postIds, ExceptionMessages exceptionMessage,
                                                  VerificationMode mode) {
        assertIllegalArgumentException(() -> postsService.deleteSelectedPosts(postIds), exceptionMessage);

        verify(postsRepository, mode).findById(anyLong());
        verify(postsRepository, never()).delete(any(Posts.class));
    }

    private void assertIllegalArgumentException(Executable executable, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.assertIllegalArgumentException(executable, exceptionMessage);
    }
}
