package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Service
public class CommentsService {
    private final CommentsRepository commentsRepository;
    private final UserService userService;
    private final PostsSearchService postsSearchService;

    @Transactional
    public Long save(CommentsSaveDto requestDto, String username) {
        User user = userService.findByUsername(username);
        Posts posts = postsSearchService.findById(requestDto.getPostsId());

        CommentsSaveDto commentsSaveDto = CommentsSaveDto.builder()
                .content(requestDto.getContent())
                .posts(posts).user(user).build();

        return commentsRepository.save(commentsSaveDto.toEntity()).getId();
    }

    @Transactional(readOnly = true)
    public List<CommentsReadDto> findByPosts(Posts posts) {
        return commentsRepository.findByPosts(posts).stream().map(CommentsReadDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAllByPosts(Posts posts) {
        commentsRepository.deleteAllByPosts(posts);
    }

    @Transactional
    public Long update(Long id, String content) {
        Comments comments = commentsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 댓글이 없습니다."));
        comments.update(content);
        return id;
    }

    @Transactional
    public void delete(Long id) {
        Comments comments = commentsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 댓글이 없습니다."));
        commentsRepository.delete(comments);
    }

    @Transactional
    public void deleteAllByUser(User user) {
        commentsRepository.deleteAllByUser(user);
    }
}
