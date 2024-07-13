package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegistrationDto {
    @NotBlank(message = "ID을(를) 입력하세요.")
    @Pattern(regexp = "^(?=[a-z])(?=.*[0-9])[a-z0-9]{8,20}", message = "ID 형식을 확인하세요.")
    private String username;

    @NotBlank(message = "비밀번호을(를) 입력하세요.")
    @Pattern(regexp = "(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9~!@#$%^&*()_+:<>?]{8,20}", message = "비밀번호 형식을 확인하세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을(를) 입력하세요.")
    private String confirmPassword;

    @NotBlank(message = "닉네임을(를) 입력하세요.")
    @Pattern(regexp = "(?=[a-zA-Z0-9가-힣])[a-zA-Z0-9가-힣]{3,10}", message = "닉네임 형식을 확인하세요.")
    private String nickname;

    @NotBlank(message = "이메일을(를) 입력하세요.")
    @Email(message = "이메일 형식을 확인하세요.")
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
