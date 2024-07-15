package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.validation.CustomEmail;
import com.eskgus.nammunity.validation.CustomNotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.EMPTY_EMAIL;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.INVALID_EMAIL;

@Getter
@NoArgsConstructor
public class EmailUpdateDto {
    @CustomNotBlank(exceptionMessage = EMPTY_EMAIL)
    @CustomEmail(exceptionMessage = INVALID_EMAIL)
    private String email;

    public EmailUpdateDto(String email) {
        this.email = email;
    }
}
