package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostsService {
    private final PostsRepository postsRepository;
    private final UserService userService;
    private final CommentsService commentsService;
    private final LikesService likesService;

    @Transactional
    public Long save(PostsSaveDto requestDto, Long id) {
        User user = userService.findById(id);
        PostsSaveDto postsSaveDto = PostsSaveDto.builder()
                .title(requestDto.getTitle()).content(requestDto.getContent())
                .user(user).build();
        return postsRepository.save(postsSaveDto.toEntity()).getId();
    }

    @Transactional
    public void countViews(Posts posts) {
        posts.countViews();
    }

    @Transactional
    public Long update(Long id, PostsUpdateDto requestDto) {
        Posts posts = postsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 게시글이 없습니다."));
        posts.update(requestDto.getTitle(), requestDto.getContent());
        return id;
    }

    @Transactional
    public void delete(Long id) {
        Posts posts = postsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 게시글이 없습니다."));
        likesService.deleteAllByPosts(posts);
        commentsService.deleteAllByPosts(posts);
        postsRepository.delete(posts);
    }

    @Transactional
    public void deleteAllByUser(User user) {
        postsRepository.findByUser(user).forEach(post -> {
                likesService.deleteAllByPosts(post);
                commentsService.deleteAllByPosts(post);
        });
        postsRepository.deleteAllByUser(user);
    }
}
