package com.eskgus.nammunity.web.dto.posts;

import com.eskgus.nammunity.domain.posts.Posts;
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

    public PostsListDto(Posts entity, int comments) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.author = entity.getUser().getNickname();
        this.modifiedDate = entity.getModifiedDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
        this.view = entity.getView();
        this.comments = comments;
    }
}
