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
    private Long comments;
    private Long likes;

    @Builder
    public PostsListDto(Posts post, Long comments, Long likes) {
        generatePosts(post);
        this.view = post.getView();
        this.comments = comments;
        this.likes = likes;
    }

    public PostsListDto(Posts post) {
        generatePosts(post);
    }

    private void generatePosts(Posts post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.author = post.getUser().getNickname();
        this.modifiedDate = DateTimeUtil.formatDateTime(post.getModifiedDate());
    }
}
