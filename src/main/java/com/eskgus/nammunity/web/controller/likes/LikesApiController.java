package com.eskgus.nammunity.web.controller.likes;

import com.eskgus.nammunity.service.likes.LikesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/likes")
public class LikesApiController {
    private final LikesService likesService;

    @PostMapping
    public Map<String, String> save(@RequestParam(required = false, name = "postsId") Long postsId,
                                    @RequestParam(required = false, name = "commentsId") Long commentsId,
                                    Principal principal) {
        log.info("postsId: " + postsId + ", commentsId: " + commentsId + ", username: " + principal.getName());

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
        log.info("postsId: " + postsId + ", commentsId: " + commentsId + ", username: " + principal.getName());

        String username = principal.getName();
        try {
            likesService.delete(postsId, commentsId, username);
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }

        return "OK";
    }
}
