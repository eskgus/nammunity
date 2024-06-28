package com.eskgus.nammunity.service.search;

import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDtos;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import org.assertj.core.util.TriFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import static com.eskgus.nammunity.domain.enums.SearchType.TITLE_AND_CONTENT;
import static com.eskgus.nammunity.util.ServiceTestUtil.createContentsPage;
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

    private static final String KEYWORDS = "keyword";
    private static final int PAGE = 1;

    @Test
    public void search() {
        // given
        Page<PostsListDto> postsPage = givePostsPage();

        Page<CommentsListDto> commentsPage = giveContentsPage(commentsService::searchByContent);

        Page<UsersListDto> usersPage = giveContentsPage(userService::searchByNickname);

        // when
        ContentsPageMoreDtos<PostsListDto, CommentsListDto, UsersListDto> result = searchService.search(KEYWORDS);

        // then
        assertEquals(postsPage, result.getContentsPageMore1().getContents());
        assertEquals(commentsPage, result.getContentsPageMore2().getContents());
        assertEquals(usersPage, result.getContentsPageMore3().getContents());

        verify(postsService).search(eq(KEYWORDS), eq(TITLE_AND_CONTENT.getKey()), anyInt(), anyInt());
        verify(commentsService).searchByContent(eq(KEYWORDS), anyInt(), anyInt());
        verify(userService).searchByNickname(eq(KEYWORDS), anyInt(), anyInt());
    }

    @Test
    public void searchPosts() {
        // given
        String searchBy = "searchBy";

        Page<PostsListDto> postsPage = givePostsPage();

        // when
        ContentsPageDto<PostsListDto> result = searchService.searchPosts(KEYWORDS, searchBy, PAGE);

        // then
        assertEquals(postsPage, result.getContents());

        verify(postsService).search(eq(KEYWORDS), eq(searchBy), eq(PAGE), anyInt());
    }

    @Test
    public void searchComments() {
        // given
        Page<CommentsListDto> commentsPage = giveContentsPage(commentsService::searchByContent);

        // when
        ContentsPageDto<CommentsListDto> result = searchService.searchComments(KEYWORDS, PAGE);

        // then
        assertEquals(commentsPage, result.getContents());

        verify(commentsService).searchByContent(eq(KEYWORDS), eq(PAGE), anyInt());
    }

    @Test
    public void searchUsers() {
        // given
        Page<UsersListDto> usersPage = giveContentsPage(userService::searchByNickname);

        // when
        ContentsPageDto<UsersListDto> result = searchService.searchUsers(KEYWORDS, PAGE);

        // then
        assertEquals(usersPage, result.getContents());

        verify(userService).searchByNickname(eq(KEYWORDS), eq(PAGE), anyInt());
    }

    private Page<PostsListDto> givePostsPage() {
        Page<PostsListDto> postsPage = createContentsPage();
        when(postsService.search(anyString(), anyString(), anyInt(), anyInt())).thenReturn(postsPage);

        return postsPage;
    }

    private <Dto> Page<Dto> giveContentsPage(TriFunction<String, Integer, Integer, Page<Dto>> finder) {
        return ServiceTestUtil.giveContentsPage(finder, String.class);
    }
}
