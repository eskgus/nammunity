package com.eskgus.nammunity.web.controller.likes;

import com.eskgus.nammunity.service.likes.LikesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/likes")
public class LikesApiController {
    private final LikesService likesService;

    @PostMapping
    public Map<String, String> save(@RequestParam(required = false, name = "postsId") Long postsId,
                                    @RequestParam(required = false, name = "commentsId") Long commentsId,
                                    Principal principal) {
        Map<String, String> response = new HashMap<>();

        String username = principal.getName();
        try {
            response.put("OK", likesService.save(postsId, commentsId, username).toString());
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }

        return response;
    }

    @DeleteMapping
    public String delete(@RequestParam(required = false, name = "postsId") Long postsId,
                         @RequestParam(required = false, name = "commentsId") Long commentsId,
                         Principal principal) {
        String username = principal.getName();
        try {
            likesService.delete(postsId, commentsId, username);
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }

        return "OK";
    }

    @DeleteMapping("/selected-delete")
    public Map<String, String> deleteSelectedLikes(@RequestBody List<Long> likesId) {
        Map<String, String> response = new HashMap<>();

        try {
            likesService.deleteSelectedLikes(likesId);
            response.put("OK", "삭제됐습니다.");
        } catch (IllegalArgumentException ex) {
            response.put("error", ex.getMessage());
        }

        return response;
    }
}
