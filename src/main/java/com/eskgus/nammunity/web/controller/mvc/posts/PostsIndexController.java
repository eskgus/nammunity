package com.eskgus.nammunity.web.controller.mvc.posts;

import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.posts.PostsViewService;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
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
    private final PostsViewService postsViewService;
    private final CommentsService commentsService;

    @GetMapping({"/", "/main"})
    public String mainPage(@RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        ContentsPageDto<PostsListDto> contentsPage = postsService.findAllDesc(page);
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
                        @RequestParam(name = "cmt", required = false) Long commentId,
                        Principal principal, Model model) {
        if (page == null) {
            if (commentId == null) {
                return readPosts(id, principal, model);
            }
            return readSpecificComments(id, commentId, principal, model);
        }
        return readComments(id, principal, page, model);
    }

    private String readPosts(Long id, Principal principal, Model model) {
        PostWithReasonsDto postWithReasons = postsViewService.readPosts(id, principal);
        model.addAttribute("postWithReasons", postWithReasons);
        return "posts/posts-read";
    }

    private String readSpecificComments(Long postId, Long commentId, Principal principal, Model model) {
        int page = commentsService.calculateCommentPage(postId, commentId);
        return readComments(postId, principal, page, model);
    }

    private String readComments(Long id, Principal principal, int page, Model model) {
        ContentsPageDto<CommentsReadDto> contentsPage = postsViewService.readComments(id, principal, page);
        model.addAttribute("contentsPage", contentsPage);
        return "posts/comments-read";
    }

    @GetMapping("/posts/update/{id}")
    public String updatePosts(@PathVariable Long id, Model model) {
        PostsUpdateDto postsUpdateDto = postsViewService.updatePosts(id);
        model.addAttribute("post", postsUpdateDto);
        return "posts/posts-update";
    }
}
