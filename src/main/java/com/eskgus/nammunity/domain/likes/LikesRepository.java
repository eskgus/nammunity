package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes, Long>, CustomLikesRepository {
    long countByUser(User user);
}
