package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomLikesRepository {
    Page<LikesListDto> findByUser(User user, Pageable pageable);
    Page<LikesListDto> findPostLikesByUser(User user, Pageable pageable);
    Page<LikesListDto> findCommentLikesByUser(User user, Pageable pageable);
    void deleteByPosts(Posts post, User user);
    void deleteByComments(Comments comment, User user);
    long countPostLikesByUser(User user);
    long countCommentLikesByUser(User user);
}
