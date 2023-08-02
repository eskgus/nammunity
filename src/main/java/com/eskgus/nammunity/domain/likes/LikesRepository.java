package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    @Query("SELECT l FROM Likes l WHERE l.posts = :posts")
    List<Likes> findByPosts(Posts posts);

    @Query("SELECT COUNT(*) FROM Likes l WHERE l.posts = :posts")
    int countByPosts(Posts posts);

    @Modifying
    @Query("DELETE FROM Likes l WHERE l.posts = :posts AND l.user = :user")
    void deleteByPosts(Posts posts, User user);

    @Modifying
    @Query("DELETE FROM Likes l WHERE l.comments = :comments AND l.user = :user")
    void deleteByComments(Comments comments, User user);

    void deleteAllByPosts(Posts posts);
    void deleteAllByUser(User user);
}
