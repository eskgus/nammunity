package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PostsSearchService {
    private final PostsRepository postsRepository;

    @Transactional(readOnly = true)
    public Page<PostsListDto> findAllDesc(int page) {
        Pageable pageable = PageRequest.of(page - 1, 20);
        return postsRepository.findAllDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Posts findById(Long id) {
        return postsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 게시글이 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<PostsListDto> findByUser(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return postsRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public long countByUser(User user) {
        return postsRepository.countByUser(user);
    }

    @Transactional(readOnly = true)
    public List<PostsListDto> searchByTitle(String keywords) {
        return postsRepository.searchByTitle(keywords);
    }

    @Transactional(readOnly = true)
    public List<PostsListDto> searchByContent(String keywords) {
        return postsRepository.searchByContent(keywords);
    }

    @Transactional(readOnly = true)
    public List<PostsListDto> searchByTitleAndContent(String keywords) {
        return postsRepository.searchByTitleAndContent(keywords);
    }
}
