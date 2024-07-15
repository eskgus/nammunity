package com.eskgus.nammunity.web.dto.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.validation.CustomNotBlank;
import com.eskgus.nammunity.validation.CustomSize;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;

@Getter
@NoArgsConstructor
public class PostsSaveDto {
    @CustomNotBlank(exceptionMessage = EMPTY_TITLE)
    @CustomSize(exceptionMessage = INVALID_TITLE, max = 100)
    private String title;

    @CustomNotBlank(exceptionMessage = EMPTY_CONTENT)
    @CustomSize(exceptionMessage = INVALID_CONTENT, max = 3000)
    private String content;

    private User user;

    @Builder
    public PostsSaveDto(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
    }

    public Posts toEntity() {
        return Posts.builder().title(title).content(content).user(user).build();
    }
}
