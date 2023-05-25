package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsReadDto;
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
    public PostsReadDto findById(Long id) {
        Posts entity = postsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 게시글이 없습니다. id = " + id));
        return new PostsReadDto(entity);
    }

    @Transactional(readOnly = true)
    public List<PostsListDto> findByUser(User user) {
        return postsRepository.findByUser(user).stream().map(PostsListDto::new).collect(Collectors.toList());
    }
}
