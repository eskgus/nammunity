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
    @NotBlank(message = "ID를 입력하세요.")
    @Pattern(regexp = "^(?=[a-z])(?=.*[0-9])[a-z0-9]{8,20}",
            message = "ID는 영어 소문자로 시작, 숫자 1개 이상 포함, 한글/특수문자/공백 불가능, 8글자 이상 20글자 이하")
    private String username;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Pattern(regexp = "(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9~!@#$%^&*()_+:<>?]{8,20}",
            message = "비밀번호는 영어와 숫자 1개 이상 포함, 특수문자 가능, 한글/공백 불가능, 8글자 이상 20글자 이하")
    private String password;

    @NotBlank(message = "비밀번호를 확인하세요.")
    private String confirmPassword;

    @NotBlank(message = "닉네임을 입력하세요.")
    @Pattern(regexp = "(?=[a-zA-Z0-9가-힣])[a-zA-Z0-9가-힣]{3,10}",
            message = "닉네임은 영어/숫자/한글 가능, 특수문자/공백 불가능, 3글자 이상 10글자 이하")
    private String nickname;

    @NotBlank(message = "이메일을 입력하세요.")
    @Email(message = "이메일 형식이 맞지 않습니다.")
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
