package com.eskgus.nammunity.web.controller.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.posts.PostsReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class PostsIndexController {
    private final PostsService postsService;
    private final PostsSearchService postsSearchService;
    private final UserService userService;
    private final CommentsService commentsService;

    @GetMapping("/")
    public String mainPage(Model model) {
        model.addAttribute("posts", postsSearchService.findAllDesc());
        return "main-page";
    }

    @GetMapping("/posts/save")
    public String savePosts() {
        return "posts/posts-save";
    }

    @GetMapping("/posts/read/{id}")
    public String readPosts(@PathVariable Long id, Principal principal, Model model) {
        Map<String, Object> attr = new HashMap<>();

        Posts posts = postsSearchService.findById(id);
        attr.put("comments", commentsService.findByPostsId(posts));

        if (!posts.getCreatedDate().equals(posts.getModifiedDate())) {
            attr.put("modify", true);
        }

        long authorId = posts.getUser().getId();
        if (principal != null
                && userService.findByUsername(principal.getName()).getId() == authorId) {
            attr.put("author", true);
        } else {
            postsService.countViews(posts);
        }

        PostsReadDto responseDto = new PostsReadDto(posts);
        attr.put("post", responseDto);

        model.addAllAttributes(attr);
        return "posts/posts-read";
    }

    @GetMapping("/posts/update/{id}")
    public String updatePosts(@PathVariable Long id, Model model) {
        Posts posts = postsSearchService.findById(id);
        PostsReadDto responseDto = new PostsReadDto(posts);
        model.addAttribute("post", responseDto);
        return "posts/posts-update";
    }
}
