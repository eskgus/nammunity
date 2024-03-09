package com.eskgus.nammunity.domain.posts;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomPostsRepository {
    List<PostsListDto> searchByTitle(String keywords);
    List<PostsListDto> searchByContent(String keywords);
    List<PostsListDto> searchByTitleAndContent(String keywords);
    Page<PostsListDto> findAllDesc(Pageable pageable);
    Page<PostsListDto> findByUser(User user, Pageable pageable);
}
