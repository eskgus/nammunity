package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostsSearchService {
    private final PostsRepository postsRepository;

    @Transactional(readOnly = true)
    public List<PostsListDto> findAllDesc() {
        return postsRepository.findAllDesc().stream().map(PostsListDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Posts findById(Long id) {
        return postsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 게시글이 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<PostsListDto> findByUser(User user) {
        return postsRepository.findByUser(user).stream().map(PostsListDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countByUser(User user) {
        return postsRepository.countByUser(user);
    }

    @Transactional(readOnly = true)
    public List<PostsListDto> searchByTitle(String keywords) {
        return postsRepository.searchByTitle(keywords).stream().map(PostsListDto::new).toList();
    }

    @Transactional(readOnly = true)
    public List<PostsListDto> searchByContent(String keywords) {
        return postsRepository.searchByContent(keywords).stream().map(PostsListDto::new).toList();
    }

    @Transactional(readOnly = true)
    public List<PostsListDto> searchByTitleAndContent(String keywords) {
        return postsRepository.searchByTitleAndContent(keywords).stream().map(PostsListDto::new).toList();
    }
}
