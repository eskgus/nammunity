package com.eskgus.nammunity.web.controller.user;

import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.ActivityService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDtos;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.ActivityHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/users")
public class UserIndexController {
    private final UserService userService;
    private final ActivityService activityService;
    private final PostsService postsService;
    private final CommentsService commentsService;
    private final LikesService likesService;
    private final LikesRepository likesRepository;

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
            model.addAttribute("exception", ex.getMessage());
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
            ContentsPageMoreDtos<PostsListDto, CommentsListDto, LikesListDto> contentsPages
                    = activityService.getMyPage(principal);
            attr.put("contentsPages", contentsPages);
        } catch (IllegalArgumentException ex) {
            attr.put("exception", ex.getMessage());
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
            model.addAttribute("exception", ex.getMessage());
            attr.put("signOut", "/users/sign-out");
        }

        model.addAllAttributes(attr);
        return "user/my-page/update-user-info";
    }

    @GetMapping("/my-page/posts")
    public String listPosts(@RequestParam(name = "page", defaultValue = "1") int page,
                            Principal principal, Model model) {
        Map<String, Object> attr = new HashMap<>();
        try {
            ContentsPageDto<PostsListDto> contentsPage = postsService.listPosts(principal, page);
            attr.put("contentsPage", contentsPage);
        } catch (IllegalArgumentException ex) {
            attr.put("exception", ex.getMessage());
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
    public String listComments(@RequestParam(name = "page", defaultValue = "1") int page,
                               Principal principal, Model model) {
        Map<String, Object> attr = new HashMap<>();
        try {
            ContentsPageDto<CommentsListDto> contentsPage = commentsService.listComments(principal, page);
            attr.put("contentsPage", contentsPage);
        } catch (IllegalArgumentException ex) {
            attr.put("exception", ex.getMessage());
            attr.put("signOut", "/users/sign-out");
        }
        model.addAllAttributes(attr);
        return "user/my-page/comments-list";
    }

    @GetMapping("/my-page/likes")
    public String listLikes(@RequestParam(name = "page", defaultValue = "1") int page,
                            Principal principal, Model model) {
        Map<String, Object> attr = new HashMap<>();
        try {
            ContentsPageDto<LikesListDto> contentsPage
                    = likesService.listLikes(likesRepository::findByUser, principal, page);
            attr.put("contentsPage", contentsPage);
        } catch (IllegalArgumentException ex) {
            attr.put("exception", ex.getMessage());
            attr.put("signOut", "/users/sign-out");
        }
        model.addAllAttributes(attr);
        return "user/my-page/likes-list";
    }

    @GetMapping("/my-page/likes/posts")
    public String listPostLikes(@RequestParam(name = "page", defaultValue = "1") int page,
                                 Principal principal, Model model) {
        Map<String, Object> attr = new HashMap<>();
        try {
            ContentsPageDto<LikesListDto> contentsPage
                    = likesService.listLikes(likesRepository::findPostLikesByUser, principal, page);
            attr.put("contentsPage", contentsPage);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("exception", ex.getMessage());
            attr.put("signOut", "/users/sign-out");
        }
        model.addAllAttributes(attr);
        return "user/my-page/likes-list-posts";
    }

    @GetMapping("/my-page/likes/comments")
    public String listCommentLikes(@RequestParam(name = "page", defaultValue = "1") int page,
                                    Principal principal, Model model) {
        Map<String, Object> attr = new HashMap<>();
        try {
            ContentsPageDto<LikesListDto> contentsPage
                    = likesService.listLikes(likesRepository::findCommentLikesByUser, principal, page);
            attr.put("contentsPage", contentsPage);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("exception", ex.getMessage());
            attr.put("signOut", "/users/sign-out");
        }
        model.addAllAttributes(attr);
        return "user/my-page/likes-list-comments";
    }

    @GetMapping("/activity-history/{type}/{id}")
    public String findActivityHistory(@PathVariable String type, @PathVariable Long id,
                                      @RequestParam(name = "page", defaultValue = "1") int page,
                                      Model model) {
        try {
            ActivityHistoryDto history = activityService.findActivityHistory(id, type, page);
            model.addAttribute("history", history);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("exception", ex.getMessage());
            // id로 user 검색 안 되면 메인("/")으로 이동 (footer)
        }
        return "user/activity-history/activity-history-" + type;
    }
}
