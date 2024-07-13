package com.eskgus.nammunity.web.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NicknameUpdateDto {
    @NotBlank(message = "닉네임을(를) 입력하세요.")
    @Pattern(regexp = "(?=[a-zA-Z0-9가-힣])[a-zA-Z0-9가-힣]{3,10}", message = "닉네임 형식을 확인하세요.")
    private String nickname;

    public NicknameUpdateDto(String nickname) {
        this.nickname = nickname;
    }
}
