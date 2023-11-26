package com.eskgus.nammunity.domain.posts;

import java.util.List;

public interface CustomPostsRepository {
    List<Posts> searchByTitle(String keywords);
    List<Posts> searchByContent(String keywords);
    List<Posts> searchByTitleAndContent(String keywords);
}
