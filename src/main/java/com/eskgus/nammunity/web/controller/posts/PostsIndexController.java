package com.eskgus.nammunity.web.controller.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.web.dto.comments.CommentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import com.eskgus.nammunity.web.dto.posts.PostWithReasonsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class PostsIndexController {
    private final PostsService postsService;
    private final PostsSearchService postsSearchService;

    @GetMapping({"/", "/main"})
    public String mainPage(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        ContentsPageDto<PostsListDto> contentsPage = postsSearchService.findAllDesc(page);
        model.addAttribute("contentsPage", contentsPage);
        return "main-page";
    }

    @GetMapping("/posts/save")
    public String savePosts() {
        return "posts/posts-save";
    }

    @GetMapping("/posts/read/{id}")
    public String read(@PathVariable Long id,
                       @RequestParam(name = "page", required = false) Integer page,
                        Principal principal, Model model) {
        if (page == null) {
            return readPosts(id, principal, model);
        }
        return readComments(id, principal, page, model);
    }

    private String readPosts(Long id, Principal principal, Model model) {
        try {
            PostWithReasonsDto postWithReasons = postsService.readPosts(id, principal);
            model.addAttribute("postWithReasons", postWithReasons);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("exception", ex.getMessage());
        }
        return "posts/posts-read";
    }

    private String readComments(Long id, Principal principal, int page, Model model) {
        try {
            CommentsPageDto commentsPage = postsService.readComments(id, principal, page);
            model.addAttribute("commentsPage", commentsPage);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("exception", ex.getMessage());
        }
        return "posts/comments-read";
    }

    @GetMapping("/posts/update/{id}")
    public String updatePosts(@PathVariable Long id, Model model) {
        try {
            Posts posts = postsSearchService.findById(id);
            PostsUpdateDto postsUpdateDto = PostsUpdateDto.builder()
                    .id(id)
                    .title(posts.getTitle())
                    .content(posts.getContent()).build();
            model.addAttribute("post", postsUpdateDto);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("exception", ex.getMessage());
        }
        return "posts/posts-update";
    }
}
