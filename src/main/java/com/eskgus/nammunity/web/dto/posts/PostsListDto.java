package com.eskgus.nammunity.web.dto.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class PostsListDto {
    private Long id;
    private String title;
    private String author;
    private String modifiedDate;
    private int view;
    private int comments;
    private int likes;

    @Builder
    public PostsListDto(Posts posts) {
        this.id = posts.getId();
        this.title = posts.getTitle();
        this.author = posts.getUser().getNickname();
        this.modifiedDate = posts.getModifiedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
        this.view = posts.getView();
        this.comments = posts.getComments().size();
        this.likes = posts.getLikes().size();
    }
}
