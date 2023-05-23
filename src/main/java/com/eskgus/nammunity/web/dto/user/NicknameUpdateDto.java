package com.eskgus.nammunity.web.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NicknameUpdateDto {
    @NotBlank(message = "닉네임을 입력하세요.")
    @Pattern(regexp = "(?=[a-zA-Z0-9가-힣])[a-zA-Z0-9가-힣]{3,10}",
            message = "닉네임은 영어/숫자/한글 가능, 특수문자/공백 불가능, 3글자 이상 10글자 이하")
    private String nickname;
}
