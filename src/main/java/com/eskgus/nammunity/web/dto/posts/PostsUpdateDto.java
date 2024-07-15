package com.eskgus.nammunity.web.dto.posts;

import com.eskgus.nammunity.validation.CustomNotBlank;
import com.eskgus.nammunity.validation.CustomSize;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;

@Getter
@NoArgsConstructor
public class PostsUpdateDto {
    private Long id;

    @CustomNotBlank(exceptionMessage = EMPTY_TITLE)
    @CustomSize(exceptionMessage = INVALID_TITLE, max = 100)
    private String title;

    @CustomNotBlank(exceptionMessage = EMPTY_CONTENT)
    @CustomSize(exceptionMessage = INVALID_CONTENT, max = 3000)
    private String content;

    @Builder
    public PostsUpdateDto(Long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }
}
