package com.eskgus.nammunity.domain.posts;

import com.eskgus.nammunity.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostsRepository extends JpaRepository<Posts, Long>, CustomPostsRepository {
    @Query("SELECT p FROM Posts p ORDER BY p.id DESC")
    List<Posts> findAllDesc();

    @Query("SELECT p FROM Posts p WHERE p.user = :user ORDER BY p.id DESC")
    List<Posts> findByUser(User user);

    long countByUser(User user);
}
