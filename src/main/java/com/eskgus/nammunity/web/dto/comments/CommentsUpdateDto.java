package com.eskgus.nammunity.web.dto.comments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentsUpdateDto {
    @NotBlank(message = "댓글을 입력하세요.")
    @Size(max = 1500, message = "댓글은 1500글자 이하여야 합니다.")
    private String content;
}
