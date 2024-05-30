package com.eskgus.nammunity.web.controller.mvc.search;

import com.eskgus.nammunity.service.search.SearchService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDtos;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/search")
public class SearchIndexController {
    private final SearchService searchService;

    @GetMapping
    public String search(@RequestParam(name = "keywords") String keywords,
                         Model model) {
        Map<String, Object> attr = new HashMap<>();

        ContentsPageMoreDtos<PostsListDto, CommentsListDto, UsersListDto> contentsPages
                = searchService.search(keywords);
        attr.put("contentsPages", contentsPages);
        attr.put("keywords", keywords);

        model.addAllAttributes(attr);
        return "search/search";
    }

    @GetMapping("/posts")
    public String searchPosts(@RequestParam(name = "keywords") String keywords,
                              @RequestParam(name = "searchBy") String searchBy,
                              @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        Map<String, Object> attr = new HashMap<>();

        ContentsPageDto<PostsListDto> contentsPage = searchService.searchPosts(keywords, searchBy, page);
        attr.put("contentsPage", contentsPage);
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

        ContentsPageDto<CommentsListDto> contentsPage = searchService.searchComments(keywords, page);
        attr.put("contentsPage", contentsPage);
        attr.put("keywords", keywords);

        model.addAllAttributes(attr);
        return "search/search-comments";
    }

    @GetMapping("/users")
    public String searchUsers(@RequestParam(name = "keywords") String keywords,
                              @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        Map<String, Object> attr = new HashMap<>();

        ContentsPageDto<UsersListDto> contentsPage = searchService.searchUsers(keywords, page);
        attr.put("contentsPage", contentsPage);
        attr.put("keywords", keywords);

        model.addAllAttributes(attr);
        return "search/search-users";
    }
}
