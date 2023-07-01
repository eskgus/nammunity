package com.eskgus.nammunity.web.dto.comments;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentsUpdateDto {
    @NotBlank(message = "댓글을 입력하세요.")
    private String content;
}
