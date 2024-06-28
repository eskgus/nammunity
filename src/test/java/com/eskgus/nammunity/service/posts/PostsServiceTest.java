package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.converter.PostsConverterForTest;
import com.eskgus.nammunity.domain.enums.SearchType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.*;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.*;
import java.util.function.BiFunction;

import static com.eskgus.nammunity.domain.enums.Fields.CONTENT;
import static com.eskgus.nammunity.domain.enums.Fields.TITLE;
import static com.eskgus.nammunity.util.ServiceTestUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostsServiceTest {
    @Mock
    private PostsRepository postsRepository;

    @Mock
    private PrincipalHelper principalHelper;

    @InjectMocks
    private PostsService postsService;

    private static final Long ID = 1L;
    private static final int PAGE = 1;
    private static final int SIZE = 3;

    @Test
    public void savePosts() {
        // given
        PostsSaveDto requestDto = PostsSaveDto.builder().title(TITLE.getKey()).content(CONTENT.getKey()).build();

        Principal principal = givePrincipal(principalHelper::getUserFromPrincipal).getFirst();

        Posts post = givePost(ID);
        when(postsRepository.save(any(Posts.class))).thenReturn(post);

        // when
        Long result = postsService.save(requestDto, principal);

        // then
        assertEquals(post.getId(), result);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(postsRepository).save(any(Posts.class));
    }

    @Test
    public void updatePosts() {
        // given
        Posts post = givePost(ID);

        PostsUpdateDto requestDto = PostsUpdateDto.builder().title(TITLE.getKey()).content(CONTENT.getKey()).build();

        when(postsRepository.findById(anyLong())).thenReturn(Optional.of(post));

        // when
        Long result = postsService.update(post.getId(), requestDto);

        // then
        assertEquals(post.getId(), result);

        verify(postsRepository).findById(eq(post.getId()));
        verify(post).update(eq(requestDto.getTitle()), eq(requestDto.getContent()));
    }

    @Test
    public void deleteSelectedPosts() {
        // given
        List<Posts> posts = givePosts();

        List<Long> postIds = createContentIds(posts, new PostsConverterForTest());

        when(postsRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return posts.stream().filter(post -> id.equals(post.getId())).findFirst();
        });

        doNothing().when(postsRepository).delete(any(Posts.class));

        // when
        postsService.deleteSelectedPosts(postIds);

        // then
        verify(postsRepository, times(postIds.size())).findById(anyLong());
        verify(postsRepository, times(posts.size())).delete(any(Posts.class));
    }

    @Test
    public void deletePosts() {
        // given
        Posts post = givePost(ID);
        when(postsRepository.findById(anyLong())).thenReturn(Optional.of(post));

        doNothing().when(postsRepository).delete(any(Posts.class));

        // when
        postsService.delete(post.getId());

        // then
        verify(postsRepository).findById(eq(post.getId()));
        verify(postsRepository).delete(eq(post));
    }

    @Test
    public void findPostsById() {
        // given
        Posts post = givePost(ID);
        when(postsRepository.findById(anyLong())).thenReturn(Optional.of(post));

        // when
        Posts result = postsService.findById(post.getId());

        // then
        assertEquals(post, result);

        verify(postsRepository).findById(eq(post.getId()));
    }

    @Test
    public void countView() {
        // given
        Posts post = mock(Posts.class);

        doNothing().when(post).countView();

        // when
        postsService.countView(post);

        // then
        verify(post).countView();
    }

    @Test
    public void findAllPostsDesc() {
        // given
        Page<PostsListDto> postsPage = giveContentsPage(postsRepository::findAllDesc);

        // when
        ContentsPageDto<PostsListDto> result = postsService.findAllDesc(PAGE);

        // then
        assertEquals(postsPage, result.getContents());

        verify(postsRepository).findAllDesc(any(Pageable.class));
    }

    @Test
    public void findPostsByUser() {
        // given
        User user = mock(User.class);

        Page<PostsListDto> postsPage = givePostsPage(postsRepository::findByUser, User.class);

        // when
        Page<PostsListDto> result = postsService.findByUser(user, PAGE, SIZE);

        // then
        assertEquals(postsPage, result);

        verify(postsRepository).findByUser(eq(user), any(Pageable.class));
    }

    @Test
    public void countPostsByUser() {
        // given
        User user = mock(User.class);

        long count = 10;
        when(postsRepository.countByUser(any(User.class))).thenReturn(count);

        // when
        long result = postsService.countByUser(user);

        // then
        assertEquals(count, result);

        verify(postsRepository).countByUser(eq(user));
    }

    @Test
    public void searchPostsByTitle() {
        testSearchPosts(SearchType.TITLE, postsRepository::searchByTitle);
    }

    @Test
    public void searchPostsByContent() {
        testSearchPosts(SearchType.CONTENT, postsRepository::searchByContent);
    }

    @Test
    public void searchPostsByTitleAndContent() {
        testSearchPosts(SearchType.TITLE_AND_CONTENT, postsRepository::searchByTitleAndContent);
    }

    private List<Posts> givePosts() {
        List<Posts> posts = new ArrayList<>();
        for (long i = 0; i < 3; i++) {
            Posts post = givePost(ID + i);
            posts.add(post);
        }

        return posts;
    }

    private Posts givePost(Long id) {
        return ServiceTestUtil.givePost(id);
    }

    private <ParamType> Page<PostsListDto> givePostsPage(BiFunction<ParamType, Pageable, Page<PostsListDto>> finder,
                                                 Class<ParamType> paramType) {
        return giveContentsPage(finder, paramType);
    }

    private void testSearchPosts(SearchType searchType, BiFunction<String, Pageable, Page<PostsListDto>> searcher) {
        // given
        String keywords = "keyword";

        Page<PostsListDto> postsPage = givePostsPage(searcher, String.class);

        List<VerificationMode> modes = setModes(searchType);

        // when
        Page<PostsListDto> result = postsService.search(keywords, searchType.getKey(), PAGE, SIZE);

        // then
        assertEquals(postsPage, result);

        verify(postsRepository, modes.get(0)).searchByTitle(eq(keywords), any(Pageable.class));
        verify(postsRepository, modes.get(1)).searchByContent(eq(keywords), any(Pageable.class));
        verify(postsRepository, modes.get(2)).searchByTitleAndContent(eq(keywords), any(Pageable.class));
    }

    private List<VerificationMode> setModes(SearchType searchType) {
        List<VerificationMode> modes = new ArrayList<>(Collections.nCopies(3, never()));

        switch (searchType) {
            case TITLE -> modes.set(0, times(1));
            case CONTENT -> modes.set(1, times(1));
            case TITLE_AND_CONTENT -> modes.set(2, times(1));
        }

        return modes;
    }
}
