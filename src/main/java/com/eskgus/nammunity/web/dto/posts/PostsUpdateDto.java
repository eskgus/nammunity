package com.eskgus.nammunity.web.dto.posts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostsUpdateDto {
    @NotBlank(message = "제목을 입력하세요.")
    @Size(max = 100, message = "글 제목은 100글자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용을 입력하세요.")
    @Size(max = 3000, message = "글 내용은 3000글자 이하여야 합니다.")
    private String content;

    @Builder
    public PostsUpdateDto(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
