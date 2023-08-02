package com.eskgus.nammunity.web.controller.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.likes.LikesSearchService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.posts.PostsReadDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class PostsIndexController {
    private final PostsService postsService;
    private final PostsSearchService postsSearchService;
    private final UserService userService;
    private final CommentsSearchService commentsSearchService;
    private final LikesSearchService likesSearchService;

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

        try {
            Posts posts = postsSearchService.findById(id);
            if (!posts.getCreatedDate().equals(posts.getModifiedDate())) {
                attr.put("modify", true);
            }

            long authorId = posts.getUser().getId();
            User user = null;
            if (principal != null) {
                user = userService.findByUsername(principal.getName());
                if (user.getId() == authorId) {
                    attr.put("pAuth", true);
                } else {
                    postsService.countViews(posts);
                }
            } else {
                postsService.countViews(posts);
            }

            List<CommentsReadDto> comments = commentsSearchService.findByPosts(posts, user);
            attr.put("comments", comments);

            PostsReadDto postsReadDto = PostsReadDto.builder()
                    .posts(posts)
                    .cSum(comments.size())
                    .likes(likesSearchService.findByPosts(posts))
                    .user(user).build();
            attr.put("post", postsReadDto);

            model.addAllAttributes(attr);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("exception", ex.getMessage());
        }
        return "posts/posts-read";
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
