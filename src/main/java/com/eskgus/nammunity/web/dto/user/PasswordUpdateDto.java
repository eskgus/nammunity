package com.eskgus.nammunity.web.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordUpdateDto {
    @NotBlank(message = "비밀번호를 입력하세요.")
    private String oldPassword;

    @NotBlank(message = "비밀번호를 입력하세요.")
    @Pattern(regexp = "(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9~!@#$%^&*()_+:<>?]{8,20}",
            message = "비밀번호는 영어와 숫자 1개 이상 포함, 특수문자 가능, 한글/공백 불가능, 8글자 이상 20글자 이하")
    private String password;

    @NotBlank(message = "비밀번호를 확인하세요.")
    private String confirmPassword;

    @Builder
    public PasswordUpdateDto(String password, String confirmPassword) {
        this.password = password;
        this.confirmPassword = confirmPassword;
    }
}
