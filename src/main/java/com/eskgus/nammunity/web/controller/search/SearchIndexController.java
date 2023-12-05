package com.eskgus.nammunity.web.controller.search;

import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import lombok.RequiredArgsConstructor;
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
    public String search(@RequestParam(name = "keywords") String keywords, Model model) {
        Map<String, Object> attr = new HashMap<>();

        List<PostsListDto> posts = postsSearchService.searchByTitleAndContent(keywords);
        if (posts.size() > 5) {
            attr.put("postsMore", true);
        }
        attr.put("posts", posts.stream().limit(5).toList());

        List<CommentsListDto> comments = commentsSearchService.searchByContent(keywords);
        if (comments.size() > 5) {
            attr.put("commentsMore", true);
        }
        attr.put("comments", comments.stream().limit(5).toList());

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
                              Model model) {
        Map<String, Object> attr = new HashMap<>();

        List<PostsListDto> posts;
        if (searchBy.equals("title")) {
            posts = postsSearchService.searchByTitle(keywords);
        } else if (searchBy.equals("content")) {
            posts = postsSearchService.searchByContent(keywords);
        } else {
            posts = postsSearchService.searchByTitleAndContent(keywords);
        }
        attr.put("posts", posts);
        attr.put(searchBy, true);
        attr.put("keywords", keywords);

        model.addAllAttributes(attr);
        return "search/search-posts";
    }

    @GetMapping("/comments")
    public String searchComments(@RequestParam(name = "keywords") String keywords, Model model) {
        Map<String, Object> attr = new HashMap<>();
        List<CommentsListDto> comments = commentsSearchService.searchByContent(keywords);
        attr.put("comments", comments);
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
