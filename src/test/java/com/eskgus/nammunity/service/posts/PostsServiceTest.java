package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.enums.SearchType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.*;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.Collections;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    public void save() {
        // given
        PostsSaveDto requestDto = PostsSaveDto.builder().title("title").content("content").build();

        Principal principal = mock(Principal.class);
        User user = mock(User.class);
        when(principalHelper.getUserFromPrincipal(principal, true)).thenReturn(user);

        Posts post = mock(Posts.class);
        when(post.getId()).thenReturn(1L);
        when(postsRepository.save(any(Posts.class))).thenReturn(post);

        // when
        Long result = postsService.save(requestDto, principal);

        // then
        assertEquals(post.getId(), result);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(postsRepository).save(any(Posts.class));
    }

    @Test
    public void saveWithoutPrincipal() {
        // given
        PostsSaveDto requestDto = PostsSaveDto.builder().title("title").content("content").build();

        when(principalHelper.getUserFromPrincipal(null, true))
                .thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> postsService.save(requestDto, null));

        verify(principalHelper).getUserFromPrincipal(null, true);

        verify(postsRepository, never()).save(any(Posts.class));
    }

    @Test
    public void findAllDesc() {
        // given
        Page<PostsListDto> postsPage = new PageImpl<>(Collections.emptyList());
        when(postsRepository.findAllDesc(any(Pageable.class))).thenReturn(postsPage);

        int page = 1;

        // when
        ContentsPageDto<PostsListDto> result = postsService.findAllDesc(page);

        // then
        assertEquals(postsPage, result.getContents());

        verify(postsRepository).findAllDesc(any(Pageable.class));
    }

    @Test
    public void findByUser() {
        // given
        Page<PostsListDto> postsPage = new PageImpl<>(Collections.emptyList());
        when(postsRepository.findByUser(any(User.class), any(Pageable.class))).thenReturn(postsPage);

        User user = mock(User.class);
        int page= 1;
        int size = 4;

        // when
        Page<PostsListDto> result = postsService.findByUser(user, page, size);

        // then
        assertEquals(postsPage, result);

        verify(postsRepository).findByUser(eq(user), any(Pageable.class));
    }

    @Test
    public void searchByTitle() {
        String keywords = testSearch(postsRepository::searchByTitle, SearchType.TITLE.getKey());

        verify(postsRepository).searchByTitle(eq(keywords), any(Pageable.class));
        verify(postsRepository, never()).searchByContent(anyString(), any(Pageable.class));
        verify(postsRepository, never()).searchByTitleAndContent(anyString(), any(Pageable.class));
    }

    @Test
    public void searchByContent() {
        String keywords = testSearch(postsRepository::searchByContent, SearchType.CONTENT.getKey());

        verify(postsRepository, never()).searchByTitle(anyString(), any(Pageable.class));
        verify(postsRepository).searchByContent(eq(keywords), any(Pageable.class));
        verify(postsRepository, never()).searchByTitleAndContent(anyString(), any(Pageable.class));
    }

    @Test
    public void searchByTitleAndContent() {
        String keywords = testSearch(postsRepository::searchByTitleAndContent, SearchType.TITLE_AND_CONTENT.getKey());

        verify(postsRepository, never()).searchByTitle(anyString(), any(Pageable.class));
        verify(postsRepository, never()).searchByContent(anyString(), any(Pageable.class));
        verify(postsRepository).searchByTitleAndContent(eq(keywords), any(Pageable.class));
    }

    private String testSearch(BiFunction<String, Pageable, Page<PostsListDto>> searcher, String searchBy) {
        // given
        Page<PostsListDto> postsPage = new PageImpl<>(Collections.emptyList());
        when(searcher.apply(anyString(), any(Pageable.class))).thenReturn(postsPage);

        String keywords = "keyword";
        int page = 1;
        int size = 4;

        // when
        Page<PostsListDto> result = postsService.search(keywords, searchBy, page, size);

        // then
        assertEquals(postsPage, result);

        return keywords;
    }
}
