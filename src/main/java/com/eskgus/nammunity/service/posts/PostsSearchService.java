package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.likes.LikesSearchService;
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
    private final CommentsSearchService commentsSearchService;
    private final LikesSearchService likesSearchService;

    @Transactional(readOnly = true)
    public List<PostsListDto> findAllDesc() {
        return postsRepository.findAllDesc().stream().map(posts -> {
                    int comments = commentsSearchService.countByPosts(posts);
                    int likes = likesSearchService.countByPosts(posts);
                    return PostsListDto.builder().posts(posts).comments(comments).likes(likes).build();
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Posts findById(Long id) {
        return postsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 게시글이 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<PostsListDto> findByUser(User user) {
        return postsRepository.findByUser(user).stream().map(posts -> {
                    int comments = commentsSearchService.countByPosts(posts);
                    int likes = likesSearchService.countByPosts(posts);
                    return PostsListDto.builder().posts(posts).comments(comments).likes(likes).build();
                }).collect(Collectors.toList());
    }
}
