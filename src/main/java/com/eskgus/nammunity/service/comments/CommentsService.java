package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CommentsService {
    private final CommentsRepository commentsRepository;
    private final UserService userService;
    private final PostsSearchService postsSearchService;
    private final LikesService likesService;

    @Transactional
    public Long save(CommentsSaveDto requestDto, String username) {
        User user = userService.findByUsername(username);
        Posts posts = postsSearchService.findById(requestDto.getPostsId());

        CommentsSaveDto commentsSaveDto = CommentsSaveDto.builder()
                .content(requestDto.getContent())
                .posts(posts).user(user).build();

        return commentsRepository.save(commentsSaveDto.toEntity()).getId();
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
        likesService.deleteAllByComments(comments);
        commentsRepository.delete(comments);
    }

    @Transactional
    public void deleteAllByPosts(Posts posts) {
        commentsRepository.findByPosts(posts).forEach(likesService::deleteAllByComments);
        commentsRepository.deleteAllByPosts(posts);
    }

    @Transactional
    public void deleteAllByUser(User user) {
        commentsRepository.findByUser(user).forEach(likesService::deleteAllByComments);
        commentsRepository.deleteAllByUser(user);
    }
}
