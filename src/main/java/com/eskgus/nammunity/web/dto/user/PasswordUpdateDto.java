package com.eskgus.nammunity.web.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordUpdateDto {
    @NotBlank(message = "현재 비밀번호을(를) 입력하세요.")
    private String oldPassword;

    @NotBlank(message = "새 비밀번호을(를) 입력하세요.")
    @Pattern(regexp = "(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9~!@#$%^&*()_+:<>?]{8,20}", message = "비밀번호 형식을 확인하세요.")
    private String password;

    @NotBlank(message = "비밀번호 확인을(를) 입력하세요.")
    private String confirmPassword;

    @Builder
    public  PasswordUpdateDto(String oldPassword, String password, String confirmPassword) {
        this.oldPassword = oldPassword;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }
}
