package com.eskgus.nammunity.web.controller.search;

import com.eskgus.nammunity.domain.enums.SearchType;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.PaginationDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/search")
public class SearchIndexController {
    private final PostsSearchService postsSearchService;
    private final CommentsSearchService commentsSearchService;
    private final UserService userService;

    @GetMapping
    public String search(@RequestParam(name = "keywords") String keywords,
                         @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        Map<String, Object> attr = new HashMap<>();
        int size = 5;

        Page<PostsListDto> posts = postsSearchService.search(keywords, SearchType.TITLE_AND_CONTENT.getKey(), page, size);
        if (posts.getTotalElements() > size) {
            attr.put("postsMore", true);
        }
        attr.put("posts", posts);

        Page<CommentsListDto> comments = commentsSearchService.searchByContent(keywords, page, size);
        if (comments.getTotalElements() > size) {
            attr.put("commentsMore", true);
        }
        attr.put("comments", comments);

        List<UsersListDto> users = userService.searchByNickname(keywords);
        if (users.size() > 5) {
            attr.put("usersMore", true);
        }
        attr.put("users", users.stream().limit(5).toList());
        attr.put("keywords", keywords);

        model.addAllAttributes(attr);
        return "search/search";
    }

    @GetMapping("/posts")
    public String searchPosts(@RequestParam(name = "keywords") String keywords,
                              @RequestParam(name = "searchBy") String searchBy,
                              @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        Map<String, Object> attr = new HashMap<>();

        Page<PostsListDto> posts = postsSearchService.search(keywords, searchBy, page, 30);
        attr.put("posts", posts);

        PaginationDto<PostsListDto> paginationDto = PaginationDto.<PostsListDto>builder()
                .page(posts).display(10).build();
        attr.put("pages", paginationDto);

        attr.put(searchBy, true);
        attr.put("searchBy", searchBy);
        attr.put("keywords", keywords);

        model.addAllAttributes(attr);
        return "search/search-posts";
    }

    @GetMapping("/comments")
    public String searchComments(@RequestParam(name = "keywords") String keywords,
                                 @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        Map<String, Object> attr = new HashMap<>();

        Page<CommentsListDto> comments = commentsSearchService.searchByContent(keywords, page, 30);
        attr.put("comments", comments);

        PaginationDto<CommentsListDto> paginationDto = PaginationDto.<CommentsListDto>builder()
                .page(comments).display(10).build();
        attr.put("pages", paginationDto);

        attr.put("keywords", keywords);

        model.addAllAttributes(attr);
        return "search/search-comments";
    }

    @GetMapping("/users")
    public String searchUsers(@RequestParam(name = "keywords") String keywords, Model model) {
        Map<String, Object> attr = new HashMap<>();
        List<UsersListDto> users = userService.searchByNickname(keywords);
        attr.put("users", users);
        attr.put("keywords", keywords);

        model.addAllAttributes(attr);
        return "search/search-users";
    }
}
