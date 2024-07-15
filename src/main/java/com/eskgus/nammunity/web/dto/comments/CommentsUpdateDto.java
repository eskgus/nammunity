package com.eskgus.nammunity.web.dto.comments;

import com.eskgus.nammunity.validation.CustomNotBlank;
import com.eskgus.nammunity.validation.CustomSize;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.EMPTY_COMMENT;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.INVALID_COMMENT;

@Getter
@NoArgsConstructor
public class CommentsUpdateDto {
    @CustomNotBlank(exceptionMessage = EMPTY_COMMENT)
    @CustomSize(exceptionMessage = INVALID_COMMENT, max = 1500)
    private String content;

    public CommentsUpdateDto(String content) {
        this.content = content;
    }
}
