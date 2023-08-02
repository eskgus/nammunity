package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.web.dto.likes.LikesReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LikesSearchService {
    private final LikesRepository likesRepository;

    @Transactional(readOnly = true)
    public List<LikesReadDto> findByPosts(Posts posts) {
        return likesRepository.findByPosts(posts).stream().map(LikesReadDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public int countByPosts(Posts posts) {
        return likesRepository.countByPosts(posts);
    }
}
