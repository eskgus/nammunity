package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.likes.LikesSaveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class LikesService {
    private final UserService userService;
    private final PostsSearchService postsSearchService;
    private final CommentsSearchService commentsSearchService;
    private final LikesRepository likesRepository;

    @Transactional
    public Long save(Long postsId, Long commentsId, String username) {
        User user = userService.findByUsername(username);

        Posts posts = null;
        Comments comments = null;
        if (postsId != null) {
            posts = postsSearchService.findById(postsId);
        } else {
            comments = commentsSearchService.findById(commentsId);
        }

        LikesSaveDto likesSaveDto = LikesSaveDto.builder()
                .posts(posts).comments(comments).user(user).build();

        return likesRepository.save(likesSaveDto.toEntity()).getId();
    }

    @Transactional
    public void delete(Long postsId, Long commentsId, String username) {
        User user = userService.findByUsername(username);

        if (postsId != null) {
            Posts posts = postsSearchService.findById(postsId);
            likesRepository.deleteByPosts(posts, user);
        } else {
            Comments comments = commentsSearchService.findById(commentsId);
            likesRepository.deleteByComments(comments, user);
        }
    }
}
