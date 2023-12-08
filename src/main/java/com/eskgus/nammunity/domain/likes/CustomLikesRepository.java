package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;

import java.util.List;

public interface CustomLikesRepository {
    List<Likes> findByUser(User user);
    List<Likes> findPostLikesByUser(User user);
    List<Likes> findCommentLikesByUser(User user);
    void deleteByPosts(Posts post, User user);
    void deleteByComments(Comments comment, User user);
    long countPostLikesByUser(User user);
    long countCommentLikesByUser(User user);
}
