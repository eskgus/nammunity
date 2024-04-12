package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikesRepository extends JpaRepository<Likes, Long>, CustomLikesRepository {
    long countByUser(User user);
    boolean existsByPostsAndUser(Posts post, User user);
    boolean existsByCommentsAndUser(Comments comment, User user);
}
