package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.validation.CustomEmail;
import com.eskgus.nammunity.validation.CustomNotBlank;
import com.eskgus.nammunity.validation.CustomPattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;

@Getter
@NoArgsConstructor
public class RegistrationDto {
    @CustomNotBlank(exceptionMessage = EMPTY_USERNAME)
    @CustomPattern(exceptionMessage = INVALID_USERNAME, regexp = "^(?=[a-z])(?=.*[0-9])[a-z0-9]{8,20}")
    private String username;

    @CustomNotBlank(exceptionMessage = EMPTY_PASSWORD)
    @CustomPattern(exceptionMessage = INVALID_PASSWORD, regexp = "(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9~!@#$%^&*()_+:<>?]{8,20}")
    private String password;

    @CustomNotBlank(exceptionMessage = EMPTY_CONFIRM_PASSWORD)
    private String confirmPassword;

    @CustomNotBlank(exceptionMessage = EMPTY_NICKNAME)
    @CustomPattern(exceptionMessage = INVALID_NICKNAME, regexp = "(?=[a-zA-Z0-9가-힣])[a-zA-Z0-9가-힣]{3,10}")
    private String nickname;

    @CustomNotBlank(exceptionMessage = EMPTY_EMAIL)
    @CustomEmail(exceptionMessage = INVALID_EMAIL)
    private String email;

    private Role role;

    @Builder
    public RegistrationDto(String username, String password, String confirmPassword, String nickname,
                           String email, Role role) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
    }

    public User toEntity() {
        return User.builder().username(username).password(password).nickname(nickname)
                .email(email).role(role).build();
    }
}
