package com.eskgus.nammunity.domain.posts;

import com.eskgus.nammunity.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostsRepository extends JpaRepository<Posts, Long>, CustomPostsRepository {
    long countByUser(User user);
}
