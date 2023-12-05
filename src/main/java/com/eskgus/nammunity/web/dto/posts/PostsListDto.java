package com.eskgus.nammunity.web.dto.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PostsListDto {
    private Long id;
    private String title;
    private String content;
    private String author;
    private String modifiedDate;
    private int view;
    private int comments;
    private int likes;

    @Builder
    public PostsListDto(Posts posts) {
        this.id = posts.getId();
        this.title = posts.getTitle();
        this.content = posts.getContent();
        this.author = posts.getUser().getNickname();
        this.modifiedDate = DateTimeUtil.formatDateTime(posts.getModifiedDate());
        this.view = posts.getView();
        this.comments = posts.getComments().size();
        this.likes = posts.getLikes().size();
    }
}
