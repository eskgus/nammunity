package com.eskgus.nammunity.web.controller.posts;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostsApiController {
    private final PostsService postsService;
    private final UserService userService;

    @PostMapping
    public Map<String, String> save(@Valid @RequestBody PostsSaveDto requestDto,
                                    Principal principal) {
        Map<String, String> response = new HashMap<>();
        User user = userService.findByUsername(principal.getName());
        response.put("OK", postsService.save(requestDto, user.getId()).toString());
        return response;
    }

    @PutMapping("/{id}")
    public Map<String, String> update(@PathVariable Long id, @Valid @RequestBody PostsUpdateDto requestDto) {
        Map<String, String> response = new HashMap<>();
        try {
            postsService.update(id, requestDto);
            response.put("OK", id.toString());
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }
        return response;
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        try {
            postsService.delete(id);
            response.put("OK", "글이 삭제되었습니다.");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }
        return response;
    }

    @DeleteMapping("/selected-delete")
    public Map<String, String> deleteSelectedPosts(@RequestBody List<Long> postsId) {
        Map<String, String> response = new HashMap<>();

        try {
            postsService.deleteSelectedPosts(postsId);
            response.put("OK", "삭제됐습니다.");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }

        return response;
    }
}
