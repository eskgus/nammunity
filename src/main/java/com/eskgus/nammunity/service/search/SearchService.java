package com.eskgus.nammunity.service.search;

import com.eskgus.nammunity.domain.enums.SearchType;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDtos;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SearchService {
    private final PostsService postsService;
    private final CommentsService commentsService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public ContentsPageMoreDtos<PostsListDto, CommentsListDto, UsersListDto> search(String keywords) {
        int page = 1;
        int size = 5;

        Page<PostsListDto> postsPage
                = postsService.search(keywords, SearchType.TITLE_AND_CONTENT.getKey(), page, size);
        ContentsPageMoreDto<PostsListDto> postsPageMoreDto = new ContentsPageMoreDto<>(postsPage);

        Page<CommentsListDto> commentsPage = commentsService.searchByContent(keywords, page, size);
        ContentsPageMoreDto<CommentsListDto> commentsPageMoreDto = new ContentsPageMoreDto<>(commentsPage);

        Page<UsersListDto> usersPage = userService.searchByNickname(keywords, page, size);
        ContentsPageMoreDto<UsersListDto> usersPageMoreDto = new ContentsPageMoreDto<>(usersPage);

        return ContentsPageMoreDtos.<PostsListDto, CommentsListDto, UsersListDto>builder()
                .contentsPageMore1(postsPageMoreDto).contentsPageMore2(commentsPageMoreDto)
                .contentsPageMore3(usersPageMoreDto).build();
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<PostsListDto> searchPosts(String keywords, String searchBy, int page) {
        Page<PostsListDto> contents = postsService.search(keywords, searchBy, page, 30);
        return new ContentsPageDto<>(contents);
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<CommentsListDto> searchComments(String keywords, int page) {
        Page<CommentsListDto> contents = commentsService.searchByContent(keywords, page, 30);
        return new ContentsPageDto<>(contents);
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<UsersListDto> searchUsers(String keywords, int page) {
        Page<UsersListDto> contents = userService.searchByNickname(keywords, page, 30);
        return new ContentsPageDto<>(contents);
    }
}
