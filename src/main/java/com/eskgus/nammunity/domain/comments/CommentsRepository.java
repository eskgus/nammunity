package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    @Query("SELECT c FROM Comments c WHERE c.posts = :posts")
    List<Comments> findByPosts(Posts posts);

    @Query("SELECT c FROM Comments c WHERE c.user = :user ORDER BY c.id DESC")
    List<Comments> findByUser(User user);

    @Query("SELECT COUNT(*) FROM Comments c WHERE c.posts = :posts")
    int countByPosts(Posts posts);

    void deleteAllByPosts(Posts posts);
    void deleteAllByUser(User user);
}
