package com.eskgus.nammunity.web.dto.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.validation.CustomNotBlank;
import com.eskgus.nammunity.validation.CustomSize;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.EMPTY_COMMENT;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.INVALID_COMMENT;

@Getter
@NoArgsConstructor
public class CommentsSaveDto {
    @CustomNotBlank(exceptionMessage = EMPTY_COMMENT)
    @CustomSize(exceptionMessage = INVALID_COMMENT, max = 1500)
    private String content;

    private Long postsId;

    private Posts posts;
    private User user;

    @Builder
    public CommentsSaveDto(String content, Posts posts, User user) {
        this.content = content;
        this.posts = posts;
        this.user = user;
    }

    public CommentsSaveDto(String content, Long postsId) {
        this.content = content;
        this.postsId = postsId;
    }

    public Comments toEntity() {
        return Comments.builder().content(content).posts(posts).user(user).build();
    }
}
