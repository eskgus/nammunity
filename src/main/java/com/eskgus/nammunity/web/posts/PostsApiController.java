package com.eskgus.nammunity.web.posts;

import com.eskgus.nammunity.domain.user.CustomUserDetails;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostsApiController {
    private final PostsService postsService;

    @PostMapping
    public Map<String, String> save(@Valid @RequestBody PostsSaveDto requestDto,
                                    @AuthenticationPrincipal CustomUserDetails user) {
        Map<String, String> response = new HashMap<>();
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
    public void delete(@PathVariable Long id) {
        postsService.delete(id);
    }
}
