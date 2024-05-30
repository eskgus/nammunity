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
        ContentsPageMoreDtos<PostsListDto, CommentsListDto, UsersListDto> contentsPages = searchService.search(keywords);
        addAttributes(new HashMap<>(), contentsPages, keywords, model);
        return "search/search";
    }

    private <T> void addAttributes(Map<String, Object> attr, T contentsPages, String keywords, Model model) {
        attr.put("contentsPage", contentsPages);
        attr.put("keywords", keywords);
        model.addAllAttributes(attr);
    }

    @GetMapping("/posts")
    public String searchPosts(@RequestParam(name = "keywords") String keywords,
                              @RequestParam(name = "searchBy") String searchBy,
                              @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        ContentsPageDto<PostsListDto> contentsPage = searchService.searchPosts(keywords, searchBy, page);

        Map<String, Object> attr = new HashMap<>();
        attr.put(searchBy, true);
        attr.put("searchBy", searchBy);
        addAttributes(attr, contentsPage, keywords, model);
        return "search/search-posts";
    }

    @GetMapping("/comments")
    public String searchComments(@RequestParam(name = "keywords") String keywords,
                                 @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        ContentsPageDto<CommentsListDto> contentsPage = searchService.searchComments(keywords, page);
        addAttributes(new HashMap<>(), contentsPage, keywords, model);
        return "search/search-comments";
    }

    @GetMapping("/users")
    public String searchUsers(@RequestParam(name = "keywords") String keywords,
                              @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        ContentsPageDto<UsersListDto> contentsPage = searchService.searchUsers(keywords, page);
        addAttributes(new HashMap<>(), contentsPage, keywords, model);
        return "search/search-users";
    }
}
