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
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@RequiredArgsConstructor
@Service
public class LikesService {
    private final UserService userService;
    private final PostsSearchService postsSearchService;
    private final CommentsSearchService commentsSearchService;
    private final LikesRepository likesRepository;

    @Transactional
    public Long save(Long postsId, Long commentsId, String username) {
        log.info("postsId: " + postsId + ", commentsId: " + commentsId + ", username: " + username);
        log.info("postsId != null: " + (postsId != null));

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
        log.info("postsId: " + postsId + ", commentsId: " + commentsId + ", username: " + username);
        log.info("postsId != null: " + (postsId != null));

        User user = userService.findByUsername(username);

        if (postsId != null) {
            Posts posts = postsSearchService.findById(postsId);
            likesRepository.deleteByPosts(posts, user);
        } else {
            Comments comments = commentsSearchService.findById(commentsId);
            likesRepository.deleteByComments(comments, user);
        }
    }

    @Transactional
    public void deleteAllByPosts(Posts posts) {
        likesRepository.deleteAllByPosts(posts);
    }

    @Transactional
    public void deleteAllByUser(User user) {
        likesRepository.deleteAllByUser(user);
    }

    @Transactional
    public void deleteAllByComments(Comments comments) {
        likesRepository.deleteAllByComments(comments);
    }
}
