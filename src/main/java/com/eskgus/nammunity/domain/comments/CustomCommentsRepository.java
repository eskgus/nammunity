package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.domain.user.User;

import java.util.List;

public interface CustomCommentsRepository {
    List<Comments> searchByContent(String keywords);
    List<Comments> findByUser(User user);
}
