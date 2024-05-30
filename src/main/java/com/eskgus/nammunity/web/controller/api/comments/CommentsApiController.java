package com.eskgus.nammunity.web.controller.api.comments;

import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import com.eskgus.nammunity.web.dto.comments.CommentsUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentsApiController {
    private final CommentsService commentsService;

    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody CommentsSaveDto requestDto,
                                  Principal principal) {
        commentsService.save(requestDto, principal);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                    @Valid @RequestBody CommentsUpdateDto requestDto) {
        commentsService.update(id, requestDto.getContent());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commentsService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/selected-delete")
    public ResponseEntity<Void> deleteSelectedComments(@RequestBody List<Long> commentIds) {
        commentsService.deleteSelectedComments(commentIds);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
