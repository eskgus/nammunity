package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.domain.posts.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentsRepository extends JpaRepository<Comments, Long> {
    @Query("SELECT c FROM Comments c WHERE c.posts = :posts")
    List<Comments> findByPosts(Posts posts);
}
