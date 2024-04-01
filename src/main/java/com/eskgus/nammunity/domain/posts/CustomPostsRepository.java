package com.eskgus.nammunity.domain.posts;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomPostsRepository {
    Page<PostsListDto> searchByTitle(String keywords, Pageable pageable);
    Page<PostsListDto> searchByContent(String keywords, Pageable pageable);
    Page<PostsListDto> searchByTitleAndContent(String keywords, Pageable pageable);
    Page<PostsListDto> findAllDesc(Pageable pageable);
    Page<PostsListDto> findByUser(User user, Pageable pageable);
}
