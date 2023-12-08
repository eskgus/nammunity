package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentsRepository extends JpaRepository<Comments, Long>, CustomCommentsRepository {
    long countByUser(User user);
}
