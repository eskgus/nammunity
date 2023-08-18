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
    private String createdDate;
    private String modifiedDate;
    private int view;
    private Long userId;
    private int cSum;
    private int lSum;
    private Boolean lAuth = null;

    @Builder
    public PostsReadDto(Posts posts, User user) {
        this.id = posts.getId();
        this.title = posts.getTitle();
        this.content = posts.getContent();
        this.author = posts.getUser().getNickname();
        this.createdDate = DateTimeUtil.formatDateTime(posts.getCreatedDate());
        this.modifiedDate = DateTimeUtil.formatModifiedDate(posts.getCreatedDate(), posts.getModifiedDate());
        this.view = posts.getView();
        this.userId = posts.getUser().getId();
        this.cSum = posts.getComments().size();
        this.lSum = posts.getLikes().size();

        posts.getLikes().forEach(like -> {
            if (like.getUser().equals(user)) {
                this.lAuth = true;
            }
        });
    }
}
