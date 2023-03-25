package com.eskgus.nammunity.web;

import com.eskgus.nammunity.service.posts.PostsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class IndexController {
    private final PostsService postsService;
    @GetMapping("/")
    public String mainPage(Model model) {
        model.addAttribute("posts", postsService.findAllDesc());
        return "main-page";
    }

    @GetMapping("/posts/save")
    public String savePosts() {
        return "posts-save";
    }

    @GetMapping("/posts/read/{id}")
    public String readPosts(@PathVariable Long id, Model model) {
        model.addAttribute("post", postsService.findById(id));
        return "posts-read";
    }
}
