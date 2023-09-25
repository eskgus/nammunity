package com.eskgus.nammunity.web.controller.comments;

import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import com.eskgus.nammunity.web.dto.comments.CommentsUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentsApiController {
    private final CommentsService commentsService;

    @PostMapping
    public Map<String, String> save(@Valid @RequestBody CommentsSaveDto requestDto,
                                    Principal principal) {
        Map<String, String> response = new HashMap<>();

        String username = principal.getName();
        try {
            commentsService.save(requestDto, username);
            response.put("OK", "댓글 작성 완료");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }

        return response;
    }

    @PutMapping("/{id}")
    public Map<String, String> update(@PathVariable Long id,
                         @Valid @RequestBody CommentsUpdateDto requestDto) {
        Map<String, String> response = new HashMap<>();

        try {
            commentsService.update(id, requestDto.getContent());
            response.put("OK", "댓글 수정 완료");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }

        return response;
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();

        try {
            commentsService.delete(id);
            response.put("OK", "삭제 완료");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }

        return response;
    }

    @DeleteMapping("/selected-delete")
    public Map<String, String> deleteSelectedComments(@RequestBody List<Long> commentsId) {
        Map<String, String> response = new HashMap<>();

        try {
            commentsService.deleteSelectedComments(commentsId);
            response.put("OK", "삭제됐습니다.");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }

        return response;
    }
}
