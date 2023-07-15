package com.eskgus.nammunity.web.dto.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentsSaveDto {
    @NotBlank(message = "댓글을 입력하세요.")
    @Size(max = 1500, message = "댓글은 1500글자 이하여야 합니다.")
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

    public Comments toEntity() {
        return Comments.builder().content(content).posts(posts).user(user).build();
    }
}
