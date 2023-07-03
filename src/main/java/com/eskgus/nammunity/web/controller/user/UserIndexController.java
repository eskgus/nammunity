package com.eskgus.nammunity.web.controller.user;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/users")
public class UserIndexController {
    private final UserService userService;
    private final PostsSearchService postsSearchService;
    private final CommentsSearchService commentsSearchService;

    @GetMapping("/sign-up")
    public String signUpUser() {
        return "user/sign-up/sign-up";
    }

    @GetMapping("/sign-up/{id}")
    public String afterSignUp(@PathVariable Long id, Model model) {
        try {
            User user = userService.findById(id);
            model.addAttribute("user", user);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("message", ex.getMessage());
        }
        return "user/sign-up/after-sign-up";
    }

    @GetMapping("/confirm-email")
    public String confirmEmail(@ModelAttribute("error") String attr, Model model) {
        if (attr.isBlank()) {
            model.addAttribute("success", "이메일 인증이 완료됐습니다.");
        } else {
            model.addAttribute("failure", attr);
        }
        return "user/sign-up/confirm-email";
    }

    @GetMapping("/sign-in")
    public String signInUser(@ModelAttribute("message") String message, Model model) {
        if (message != null) {
            model.addAttribute("message", message);
        }
        return "user/sign-in/sign-in";
    }

    @GetMapping("/find/username")
    public String findUsername() {
        return "user/sign-in/find-username";
    }

    @GetMapping("/find/password")
    public String findPassword() {
        return "user/sign-in/find-password";
    }

    @GetMapping("/my-page")
    public String myPage(Principal principal, Model model) {
        Map<String, Object> attr = new HashMap<>();
        try {
            User user = userService.findByUsername(principal.getName());

            List<PostsListDto> posts = postsSearchService.findByUser(user);
            if (posts.size() > 5) {
                attr.put("postsMore", true);
            }
            attr.put("posts", posts.stream().limit(5).collect(Collectors.toList()));

            List<CommentsListDto> comments = commentsSearchService.findByUser(user);
            if (comments.size() > 5) {
                attr.put("commentsMore", true);
            }
            attr.put("comments", comments.stream().limit(5).collect(Collectors.toList()));
        } catch (IllegalArgumentException ex) {
            attr.put("error", ex.getMessage());
            attr.put("signOut", "/users/sign-out");
        }
        model.addAllAttributes(attr);
        return "user/my-page/my-page";
    }

    @GetMapping("/my-page/update/password")
    public String updatePassword() {
        return "user/my-page/update-password";
    }

    @GetMapping("/my-page/update/user-info")
    public String updateUserInfo(Principal principal, Model model) {
        Map<String, Object> attr = new HashMap<>();

        try {
            User user = userService.findByUsername(principal.getName());
            attr.put("user", user);
            attr.put(user.getSocial(), true);
        } catch (IllegalArgumentException ex) {
            attr.put("error", ex.getMessage());
            attr.put("signOut", "/users/sign-out");
        }

        model.addAllAttributes(attr);
        return "user/my-page/update-user-info";
    }

    @GetMapping("/my-page/posts")
    public String listPosts(Principal principal, Model model) {
        Map<String, Object> attr = new HashMap<>();
        try {
            User user = userService.findByUsername(principal.getName());
            attr.put("posts", postsSearchService.findByUser(user));
        } catch (IllegalArgumentException ex) {
            attr.put("error", ex.getMessage());
            attr.put("signOut", "/users/sign-out");
        }
        model.addAllAttributes(attr);
        return "user/my-page/posts-list";
    }

    @GetMapping("/my-page/delete/account")
    public String deleteAccount() {
        return "user/my-page/delete-account";
    }

    @GetMapping("/my-page/comments")
    public String listComments(Principal principal, Model model) {
        Map<String, Object> attr = new HashMap<>();
        try {
            User user = userService.findByUsername(principal.getName());
            attr.put("comments", commentsSearchService.findByUser(user));
        } catch (IllegalArgumentException ex) {
            attr.put("error", ex.getMessage());
            attr.put("signOut", "/users/sign-out");
        }
        model.addAllAttributes(attr);
        return "user/my-page/comments-list";
    }
}
