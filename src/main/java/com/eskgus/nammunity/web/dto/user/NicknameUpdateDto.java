package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.validation.CustomNotBlank;
import com.eskgus.nammunity.validation.CustomPattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.EMPTY_NICKNAME;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.INVALID_NICKNAME;

@Getter
@NoArgsConstructor
public class NicknameUpdateDto {
    @CustomNotBlank(exceptionMessage = EMPTY_NICKNAME)
    @CustomPattern(exceptionMessage = INVALID_NICKNAME, regexp = "(?=[a-zA-Z0-9가-힣])[a-zA-Z0-9가-힣]{3,10}")
    private String nickname;

    public NicknameUpdateDto(String nickname) {
        this.nickname = nickname;
    }
}
