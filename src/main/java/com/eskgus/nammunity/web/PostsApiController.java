package com.eskgus.nammunity.web;


import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.web.dto.PostsSaveRequestDto;
import com.eskgus.nammunity.web.dto.PostsUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class PostsApiController {
    private final PostsService postsService;

    @PostMapping("/api/posts")
    public Long save(@RequestBody PostsSaveRequestDto requestDto) {
        return postsService.save(requestDto);
    }

    @PutMapping("/api/posts/{id}")
    public Long update(@PathVariable Long id, @RequestBody PostsUpdateRequestDto requestDto) {
        return postsService.update(id, requestDto);
    }

    @DeleteMapping("/api/posts/{id}")
    public void delete(@PathVariable Long id) {
        postsService.delete(id);
    }
}
