package com.eskgus.nammunity.web.controller.mvc.user;

import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.service.comments.CommentsViewService;
import com.eskgus.nammunity.service.likes.LikesViewService;
import com.eskgus.nammunity.service.posts.PostsViewService;
import com.eskgus.nammunity.service.user.UserViewService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDtos;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.user.ActivityHistoryDto;
import com.eskgus.nammunity.web.dto.user.UserUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/users")
public class UserIndexController {
    private final UserViewService userViewService;
    private final PostsViewService postsViewService;
    private final CommentsViewService commentsViewService;
    private final LikesViewService likesViewService;
    private final LikesRepository likesRepository;

    @GetMapping("/sign-up")
    public String signUpUser() {
        return "user/sign-up/sign-up";
    }

    @GetMapping("/sign-up/{id}")
    public String afterSignUp(@PathVariable Long id, Model model) {
        UserUpdateDto userUpdateDto = userViewService.afterSignUp(id);
        model.addAttribute("user", userUpdateDto);
        return "user/sign-up/after-sign-up";
    }

    @GetMapping("/confirm-email")
    public String confirmEmail(@ModelAttribute("error") String error, Model model) {
        String message = error.isBlank() ? "이메일 인증이 완료됐습니다." : error;
        model.addAttribute("message", message);
        return "user/sign-up/confirm-email";
    }

    @GetMapping("/sign-in")
    public String signInUser(@ModelAttribute("message") String message, Model model) {
        model.addAttribute("message", message);
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
        ContentsPageMoreDtos<PostsListDto, CommentsListDto, LikesListDto> contentsPages
                = userViewService.getMyPage(principal);
        model.addAttribute("contentsPages", contentsPages);
        return "user/my-page/my-page";
    }

    @GetMapping("/my-page/update/password")
    public String updatePassword() {
        return "user/my-page/update-password";
    }

    @GetMapping("/my-page/update/user-info")
    public String updateUserInfo(Principal principal, Model model) {
        UserUpdateDto userUpdateDto = userViewService.updateUserInfo(principal);
        model.addAttribute("user", userUpdateDto);
        return "user/my-page/update-user-info";
    }

    @GetMapping("/my-page/posts")
    public String listPosts(@RequestParam(name = "page", defaultValue = "1") int page,
                            Principal principal, Model model) {
        ContentsPageDto<PostsListDto> contentsPage = postsViewService.listPosts(principal, page);
        model.addAttribute("contentsPage", contentsPage);
        return "user/my-page/posts-list";
    }

    @GetMapping("/my-page/delete/account")
    public String deleteAccount() {
        return "user/my-page/delete-account";
    }

    @GetMapping("/my-page/comments")
    public String listComments(@RequestParam(name = "page", defaultValue = "1") int page,
                               Principal principal, Model model) {
        ContentsPageDto<CommentsListDto> contentsPage = commentsViewService.listComments(principal, page);
        model.addAttribute("contentsPage", contentsPage);
        return "user/my-page/comments-list";
    }

    @GetMapping("/my-page/likes")
    public String listLikes(@RequestParam(name = "page", defaultValue = "1") int page,
                            Principal principal, Model model) {
        ContentsPageDto<LikesListDto> contentsPage
                = likesViewService.listLikes(likesRepository::findByUser, principal, page);
        model.addAttribute("contentsPage", contentsPage);
        return "user/my-page/likes-list";
    }

    @GetMapping("/my-page/likes/posts")
    public String listPostLikes(@RequestParam(name = "page", defaultValue = "1") int page,
                                Principal principal, Model model) {
        ContentsPageDto<LikesListDto> contentsPage
                = likesViewService.listLikes(likesRepository::findPostLikesByUser, principal, page);
        model.addAttribute("contentsPage", contentsPage);
        return "user/my-page/likes-list-posts";
    }

    @GetMapping("/my-page/likes/comments")
    public String listCommentLikes(@RequestParam(name = "page", defaultValue = "1") int page,
                                   Principal principal, Model model) {
        ContentsPageDto<LikesListDto> contentsPage
                = likesViewService.listLikes(likesRepository::findCommentLikesByUser, principal, page);
        model.addAttribute("contentsPage", contentsPage);
        return "user/my-page/likes-list-comments";
    }

    @GetMapping("/activity-history/{type}/{id}")
    public String findActivityHistory(@PathVariable String type, @PathVariable Long id,
                                      @RequestParam(name = "page", defaultValue = "1") int page,
                                      Model model) {
        ActivityHistoryDto history = userViewService.findActivityHistory(id, type, page);
        model.addAttribute("history", history);
        return "user/activity-history/activity-history-" + type;
    }
}
