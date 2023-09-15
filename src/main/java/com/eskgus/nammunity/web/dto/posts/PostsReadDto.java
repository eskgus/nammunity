package com.eskgus.nammunity.web.dto.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostsReadDto {
    private Long id;
    private String title;
    private String content;
    private String author;
    private Long authorId;
    private String createdDate;
    private String modifiedDate;
    private int view;
    private int cSum;   // 댓글 개수
    private int lSum;   // 좋아요 개수
    private Boolean lAuth;   // 좋아요 누른 사용자 확인

    @Builder
    public PostsReadDto(Posts posts, User user) {
        this.id = posts.getId();
        this.title = posts.getTitle();
        this.content = posts.getContent();
        this.author = posts.getUser().getNickname();
        this.authorId = posts.getUser().getId();
        this.createdDate = DateTimeUtil.formatDateTime(posts.getCreatedDate());
        this.modifiedDate = DateTimeUtil.formatModifiedDate(posts.getCreatedDate(), posts.getModifiedDate());
        this.view = posts.getView();
        this.cSum = posts.getComments().size();
        this.lSum = posts.getLikes().size();

        posts.getLikes().forEach(like -> {
            if (like.getUser().equals(user)) {
                this.lAuth = true;
            }
        });
    }
}
