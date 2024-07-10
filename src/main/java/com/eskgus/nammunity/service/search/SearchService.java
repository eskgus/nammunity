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

        Page<PostsListDto> postsPage = searchPostsBySearchType(
                keywords, SearchType.TITLE_AND_CONTENT.getKey(), page, size);
        ContentsPageMoreDto<PostsListDto> postsPageMoreDto = createContentsPageMoreDto(postsPage);

        Page<CommentsListDto> commentsPage = searchCommentsByContent(keywords, page, size);
        ContentsPageMoreDto<CommentsListDto> commentsPageMoreDto = createContentsPageMoreDto(commentsPage);

        Page<UsersListDto> usersPage = searchUsersByNickname(keywords, page, size);
        ContentsPageMoreDto<UsersListDto> usersPageMoreDto = createContentsPageMoreDto(usersPage);

        return ContentsPageMoreDtos.<PostsListDto, CommentsListDto, UsersListDto>builder()
                .contentsPageMore1(postsPageMoreDto).contentsPageMore2(commentsPageMoreDto)
                .contentsPageMore3(usersPageMoreDto).build();
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<PostsListDto> searchPosts(String keywords, String searchBy, int page) {
        Page<PostsListDto> postsPage = searchPostsBySearchType(keywords, searchBy, page, 30);
        return createContentsPageDto(postsPage);
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<CommentsListDto> searchComments(String keywords, int page) {
        Page<CommentsListDto> commentsPage = searchCommentsByContent(keywords, page, 30);
        return createContentsPageDto(commentsPage);
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<UsersListDto> searchUsers(String keywords, int page) {
        Page<UsersListDto> usersPage = searchUsersByNickname(keywords, page, 30);
        return createContentsPageDto(usersPage);
    }

    private Page<PostsListDto> searchPostsBySearchType(String keywords, String searchBy, int page, int size) {
        return postsService.search(keywords, searchBy, page, size);
    }

    private Page<CommentsListDto> searchCommentsByContent(String keywords, int page, int size) {
        return commentsService.searchByContent(keywords, page, size);
    }

    private Page<UsersListDto> searchUsersByNickname(String keywords, int page, int size) {
        return userService.searchByNickname(keywords, page, size);
    }

    private <Dto> ContentsPageMoreDto<Dto> createContentsPageMoreDto(Page<Dto> contentsPage) {
        return new ContentsPageMoreDto<>(contentsPage);
    }

    private <Dto> ContentsPageDto<Dto> createContentsPageDto(Page<Dto> contentsPage) {
        return new ContentsPageDto<>(contentsPage);
    }
}
