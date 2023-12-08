package com.eskgus.nammunity.domain.posts;

import com.eskgus.nammunity.domain.user.User;

import java.util.List;

public interface CustomPostsRepository {
    List<Posts> searchByTitle(String keywords);
    List<Posts> searchByContent(String keywords);
    List<Posts> searchByTitleAndContent(String keywords);
    List<Posts> findAllDesc();
    List<Posts> findByUser(User user);
}
