package com.eskgus.nammunity.service.search;

import com.eskgus.nammunity.domain.enums.SearchType;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDtos;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {
    @Mock
    private PostsService postsService;

    @Mock
    private CommentsService commentsService;

    @Mock
    private UserService userService;

    @InjectMocks
    private SearchService searchService;

    @Test
    public void search() {
        // given
        Page<PostsListDto> postsPage = new PageImpl<>(Collections.emptyList());
        when(postsService.search(anyString(), eq(SearchType.TITLE_AND_CONTENT.getKey()), anyInt(), anyInt()))
                .thenReturn(postsPage);

        Page<CommentsListDto> commentsPage = new PageImpl<>(Collections.emptyList());
        when(commentsService.searchByContent(anyString(), anyInt(), anyInt())).thenReturn(commentsPage);

        Page<UsersListDto> usersPage = new PageImpl<>(Collections.emptyList());
        when(userService.searchByNickname(anyString(), anyInt(), anyInt())).thenReturn(usersPage);

        String keywords = "keyword";

        // when
        ContentsPageMoreDtos<PostsListDto, CommentsListDto, UsersListDto> result
                = searchService.search(keywords);

        // then
        assertEquals(postsPage, result.getContentsPageMore1().getContents());
        assertEquals(commentsPage, result.getContentsPageMore2().getContents());
        assertEquals(usersPage, result.getContentsPageMore3().getContents());

        verify(postsService).search(eq(keywords), eq(SearchType.TITLE_AND_CONTENT.getKey()), anyInt(), anyInt());
        verify(commentsService).searchByContent(eq(keywords), anyInt(), anyInt());
        verify(userService).searchByNickname(eq(keywords), anyInt(), anyInt());
    }

    @Test
    public void searchPosts() {
        // given
        Page<PostsListDto> postsPage = new PageImpl<>(Collections.emptyList());
        when(postsService.search(anyString(), anyString(), anyInt(), anyInt())).thenReturn(postsPage);

        String keywords = "keyword";
        String searchBy = "search by";
        int page = 1;

        // when
        ContentsPageDto<PostsListDto> result = searchService.searchPosts(keywords, searchBy, page);

        // then
        assertEquals(postsPage, result.getContents());

        verify(postsService).search(eq(keywords), eq(searchBy), eq(page), anyInt());
    }

    @Test
    public void searchComments() {
        // given
        Page<CommentsListDto> commentsPage = new PageImpl<>(Collections.emptyList());
        when(commentsService.searchByContent(anyString(), anyInt(), anyInt())).thenReturn(commentsPage);

        String keywords = "keyword";
        int page = 1;

        // when
        ContentsPageDto<CommentsListDto> result = searchService.searchComments(keywords, page);

        // then
        assertEquals(commentsPage, result.getContents());

        verify(commentsService).searchByContent(eq(keywords), eq(page), anyInt());
    }

    @Test
    public void searchUsers() {
        // given
        Page<UsersListDto> usersPage = new PageImpl<>(Collections.emptyList());
        when(userService.searchByNickname(anyString(), anyInt(), anyInt())).thenReturn(usersPage);

        String keywords = "keyword";
        int page = 1;

        // when
        ContentsPageDto<UsersListDto> result = searchService.searchUsers(keywords, page);

        // then
        assertEquals(usersPage, result.getContents());

        verify(userService).searchByNickname(eq(keywords), eq(page), anyInt());
    }
}
