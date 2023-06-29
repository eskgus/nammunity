package com.eskgus.nammunity.web.controller.comments;

import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentsApiController {
    private final CommentsService commentsService;

    @PostMapping
    public Map<String, String> save(@Valid @RequestBody CommentsSaveDto requestDto,
                                    Principal principal) {
        log.info("comments save.....");

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
}
