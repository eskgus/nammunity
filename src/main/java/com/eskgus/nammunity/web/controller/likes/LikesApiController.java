package com.eskgus.nammunity.web.controller.likes;

import com.eskgus.nammunity.service.likes.LikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/likes")
public class LikesApiController {
    private final LikesService likesService;

    @PostMapping
    public ResponseEntity<Void> save(@RequestParam(required = false, name = "postsId") Long postsId,
                                     @RequestParam(required = false, name = "commentsId") Long commentsId,
                                     Principal principal) {
        likesService.save(postsId, commentsId, principal);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam(required = false, name = "postsId") Long postsId,
                                       @RequestParam(required = false, name = "commentsId") Long commentsId,
                                       Principal principal) {
        likesService.deleteByContentId(postsId, commentsId, principal);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/selected-delete")
    public ResponseEntity<Void> deleteSelectedLikes(@RequestBody List<Long> likeIds) {
        likesService.deleteSelectedLikes(likeIds);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
