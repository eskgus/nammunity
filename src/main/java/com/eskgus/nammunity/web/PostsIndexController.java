package com.eskgus.nammunity.web;

import com.eskgus.nammunity.domain.user.CustomUserDetails;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.web.dto.posts.PostsReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class PostsIndexController {
    private final PostsService postsService;

    @GetMapping("/")
    public String mainPage(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        if (user != null) {
            model.addAttribute("user", user.getNickname());
        }
        model.addAttribute("posts", postsService.findAllDesc());
        return "main-page";
    }

    @GetMapping("/posts/save")
    public String savePosts(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        if (user != null) {
            model.addAttribute("user", user.getNickname());
        }
        return "posts/posts-save";
    }

    @GetMapping("/posts/read/{id}")
    public String readPosts(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user, Model model) {
        if (user != null) {
            model.addAttribute("user", user.getNickname());
        }
        postsService.countViews(id);
        PostsReadDto responseDto = postsService.findById(id);
        model.addAttribute("post", responseDto);
        return "posts/posts-read";
    }

    @GetMapping("/posts/update/{id}")
    public String updatePosts(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user, Model model) {
        if (user != null) {
            model.addAttribute("user", user.getNickname());
        }
        PostsReadDto responseDto = postsService.findById(id);
        model.addAttribute("post", responseDto);
        return "posts/posts-update";
    }
}
