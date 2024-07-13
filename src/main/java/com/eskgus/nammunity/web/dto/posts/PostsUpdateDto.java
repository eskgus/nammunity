package com.eskgus.nammunity.web.dto.posts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostsUpdateDto {
    private Long id;

    @NotBlank(message = "제목을(를) 입력하세요.")
    @Size(max = 100, message = "제목은 100글자 이하로 작성하세요.")
    private String title;

    @NotBlank(message = "내용을(를) 입력하세요.")
    @Size(max = 3000, message = "내용은 3000글자 이하로 작성하세요.")
    private String content;

    @Builder
    public PostsUpdateDto(Long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }
}
