package com.eskgus.nammunity.web.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailUpdateDto {
    @NotBlank(message = "이메일을(를) 입력하세요.")
    @Email(message = "이메일 형식을 확인하세요.")
    private String email;

    public EmailUpdateDto(String email) {
        this.email = email;
    }
}
