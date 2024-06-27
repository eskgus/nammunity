package com.eskgus.nammunity.web.dto.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import static com.eskgus.nammunity.util.DateTimeUtil.formatDateTime;
import static com.eskgus.nammunity.util.DateTimeUtil.formatModifiedDate;

@Getter
public class PostsReadDto {
    private final Long id;
    private String author;
    private Long authorId;
    private final String title;
    private final String content;
    private final String createdDate;
    private final String modifiedDate;
    private final int view;
    private final boolean postedByUser;
    private final int likes;
    private final boolean likedByUser;

    @Builder
    public PostsReadDto(Posts post, boolean postedByUser, boolean likedByUser) {
        this.id = post.getId();
        generateAuthor(post.getUser());
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdDate = formatDateTime(post.getCreatedDate());
        this.modifiedDate = formatModifiedDate(post.getCreatedDate(), post.getModifiedDate());
        this.view = post.getView();
        this.postedByUser = postedByUser;
        this.likes = post.getLikes().size();
        this.likedByUser = likedByUser;
    }

    private void generateAuthor(User user) {
        this.author = user.getNickname();
        this.authorId = user.getId();
    }
}
