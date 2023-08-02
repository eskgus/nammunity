package com.eskgus.nammunity.web.dto.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.likes.LikesReadDto;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
    public PostsReadDto(Posts posts, int cSum, List<LikesReadDto> likes, User user) {
        this.id = posts.getId();
        this.title = posts.getTitle();
        this.content = posts.getContent();
        this.author = posts.getUser().getNickname();
        this.createdDate = posts.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
        this.modifiedDate = posts.getModifiedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
        this.view = posts.getView();
        this.userId = posts.getUser().getId();
        this.cSum = cSum;
        this.lSum = likes.size();

        likes.forEach(like -> {
            if (like.getUser().equals(user)) {
                this.lAuth = true;
            }
        });
    }
}
