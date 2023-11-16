package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    @Query("SELECT l FROM Likes l WHERE l.user = :user ORDER BY l.id DESC")
    List<Likes> findByUser(User user);

    @Query("SELECT l FROM Likes l WHERE l.user = :user AND l.posts IS NOT NULL ORDER BY l.id DESC")
    List<Likes> findPostsByUser(User user);

    @Query("SELECT l FROM Likes l WHERE l.user = :user AND l.comments IS NOT NULL ORDER BY l.id DESC")
    List<Likes> findCommentsByUser(User user);

    @Modifying
    @Query("DELETE FROM Likes l WHERE l.posts = :posts AND l.user = :user")
    void deleteByPosts(Posts posts, User user);

    @Modifying
    @Query("DELETE FROM Likes l WHERE l.comments = :comments AND l.user = :user")
    void deleteByComments(Comments comments, User user);

    long countByUser(User user);

    @Query("SELECT COUNT(l) FROM Likes l WHERE l.user = :user AND l.posts IS NOT NULL")
    long countPostLikesByUser(User user);

    @Query("SELECT COUNT(l) FROM Likes l WHERE l.user = :user AND l.comments IS NOT NULL")
    long countCommentLikesByUser(User user);
}
