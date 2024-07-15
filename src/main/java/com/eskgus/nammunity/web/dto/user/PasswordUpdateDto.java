package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.validation.CustomNotBlank;
import com.eskgus.nammunity.validation.CustomPattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;

@Getter
@NoArgsConstructor
public class PasswordUpdateDto {
    @CustomNotBlank(exceptionMessage = EMPTY_OLD_PASSWORD)
    private String oldPassword;

    @CustomNotBlank(exceptionMessage = EMPTY_NEW_PASSWORD)
    @CustomPattern(exceptionMessage = INVALID_PASSWORD, regexp = "(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9~!@#$%^&*()_+:<>?]{8,20}")
    private String password;

    @CustomNotBlank(exceptionMessage = EMPTY_CONFIRM_PASSWORD)
    private String confirmPassword;

    @Builder
    public  PasswordUpdateDto(String oldPassword, String password, String confirmPassword) {
        this.oldPassword = oldPassword;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }
}
