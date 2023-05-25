package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.domain.user.CustomUserDetails;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
@RequestMapping("/users")
public class UserIndexController {
    private final UserService userService;
    private final PostsSearchService postsSearchService;

    @GetMapping("/sign-up")
    public String signUpUser() {
        return "user/sign-up";
    }

    @GetMapping("/sign-up/{id}")
    public String afterSignUp(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "user/after-sign-up";
    }

    @GetMapping("/confirm-email")
    public String confirmEmail(@ModelAttribute("error") String attr, Model model) {
        if (attr.isBlank()) {
            model.addAttribute("success", "이메일 인증이 완료됐습니다.");
        } else {
            model.addAttribute("error", attr);
        }
        return "user/confirm-email";
    }

    @GetMapping("/sign-in")
    public String signInUser(@ModelAttribute("message") String message, Model model) {
        if (message != null) {
            model.addAttribute("message", message);
        }
        return "user/sign-in";
    }

    @GetMapping("/find/username")
    public String findUsername() {
        return "user/find-username";
    }

    @GetMapping("/find/password")
    public String findPassword() {
        return "user/find-password";
    }

    @GetMapping("/my-page")
    public String myPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId());
        model.addAttribute("posts", postsSearchService.findByUser(user));
        return "user/my-page";
    }

    @GetMapping("/my-page/update/password")
    public String updatePassword() {
        return "user/update-password";
    }

    @GetMapping("/my-page/update/user-info")
    public String updateUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId());
        model.addAttribute("user", user);
        return "user/update-user-info";
    }
}
