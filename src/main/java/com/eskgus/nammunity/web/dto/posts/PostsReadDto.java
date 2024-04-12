package com.eskgus.nammunity.web.dto.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import static com.eskgus.nammunity.util.DateTimeUtil.formatDateTime;
import static com.eskgus.nammunity.util.DateTimeUtil.formatModifiedDate;

@Getter
public class PostsReadDto {
    private Long id;
    private String author;
    private Long authorId;
    private String title;
    private String content;
    private String createdDate;
    private String modifiedDate;
    private int view;
    private boolean doesUserWritePost;
    private int likes;
    private boolean doesUserLikePost;

    @Builder
    public PostsReadDto(Posts post, boolean doesUserWritePost, boolean doesUserLikePost) {
        this.id = post.getId();
        generateAuthor(post.getUser());
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdDate = formatDateTime(post.getCreatedDate());
        this.modifiedDate = formatModifiedDate(post.getCreatedDate(), post.getModifiedDate());
        this.view = post.getView();
        this.doesUserWritePost = doesUserWritePost;
        this.likes = post.getLikes().size();
        this.doesUserLikePost = doesUserLikePost;
    }

    private void generateAuthor(User user) {
        this.author = user.getNickname();
        this.authorId = user.getId();
    }
}
