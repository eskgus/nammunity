package com.eskgus.nammunity.web.posts;

import com.eskgus.nammunity.domain.user.CustomUserDetails;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.web.dto.posts.PostsReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class PostsIndexController {
    private final PostsService postsService;

    @GetMapping("/")
    public String mainPage(Model model) {
        model.addAttribute("posts", postsService.findAllDesc());
        return "main-page";
    }

    @GetMapping("/posts/save")
    public String savePosts(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        return "posts/posts-save";
    }

    @GetMapping("/posts/read/{id}")
    public String readPosts(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user, Model model) {
        Map<String, Object> attr = new HashMap<>();

        postsService.countViews(id);
        PostsReadDto responseDto = postsService.findById(id);
        attr.put("post", responseDto);

        Long authorId = responseDto.getUserId();
        if (user != null && user.getId().equals(authorId)) {
            attr.put("author", true);
        }

        model.addAllAttributes(attr);
        return "posts/posts-read";
    }

    @GetMapping("/posts/update/{id}")
    public String updatePosts(@PathVariable Long id, Model model) {
        PostsReadDto responseDto = postsService.findById(id);
        model.addAttribute("post", responseDto);
        return "posts/posts-update";
    }
}
