package com.eskgus.nammunity.web.controller.api.user;

import com.eskgus.nammunity.service.user.SignInService;
import com.eskgus.nammunity.validation.CustomEmail;
import com.eskgus.nammunity.validation.CustomNotBlank;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users/sign-in")
public class SignInApiController {
    private final SignInService signInService;

    @GetMapping("/username")
    public ResponseEntity<String> findUsername(@RequestParam @CustomNotBlank(exceptionMessage = EMPTY_EMAIL)
                                                   @CustomEmail(exceptionMessage = INVALID_EMAIL) String email) {
        String username = signInService.findUsername(email);
        return ResponseEntity.status(HttpStatus.OK).body(username);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> findPassword(@RequestParam @CustomNotBlank(exceptionMessage = EMPTY_USERNAME) String username,
                                            HttpServletRequest request) {
        signInService.findPassword(username);
        request.getSession().setAttribute("prePage", "/users/my-page/update/password");
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
