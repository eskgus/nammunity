package com.eskgus.nammunity.web.controller.api.posts;

import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostsApiController {
    private final PostsService postsService;

    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody PostsSaveDto requestDto,
                                     Principal principal) {
        postsService.save(requestDto, principal);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @Valid @RequestBody PostsUpdateDto requestDto) {
        postsService.update(id, requestDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postsService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/selected-delete")
    public ResponseEntity<Void> deleteSelectedPosts(@RequestBody List<Long> postIds) {
        postsService.deleteSelectedPosts(postIds);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
