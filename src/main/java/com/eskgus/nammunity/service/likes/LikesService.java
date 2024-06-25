package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import com.eskgus.nammunity.web.dto.likes.LikesSaveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.function.BiFunction;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.EMPTY_CONTENT_IDS;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.LIKE_NOT_FOUND;
import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;

@RequiredArgsConstructor
@Service
public class LikesService {
    private final PostsService postsService;
    private final CommentsService commentsService;
    private final LikesRepository likesRepository;
    private final PrincipalHelper principalHelper;

    @Transactional
    public Long save(Long postsId, Long commentsId, Principal principal) {
        User user = principalHelper.getUserFromPrincipal(principal, true);

        LikesSaveDto likesSaveDto = createLikesSaveDto(postsId, commentsId, user);

        return likesRepository.save(likesSaveDto.toEntity()).getId();
    }

    @Transactional
    public void deleteByContentId(Long postsId, Long commentsId, Principal principal) {
        User user = principalHelper.getUserFromPrincipal(principal, true);

        if (postsId != null) {
            deleteByPostId(postsId, user);
        } else {
            deleteByCommentId(commentsId, user);
        }
    }

    @Transactional
    public void deleteSelectedLikes(List<Long> likeIds) {
        if (likeIds.isEmpty()) {
            throw new IllegalArgumentException(EMPTY_CONTENT_IDS.getMessage());
        }

        likeIds.forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public Page<LikesListDto> findLikesByUser(User user, BiFunction<User, Pageable, Page<LikesListDto>> finder,
                                              int page, int size) {
        // finder: likesRepository.findByUser(전체 좋아요), findPostLikesByUser(게시글 좋아요), findCommentLikesByUser(댓글 좋아요)
        Pageable pageable = createPageable(page, size);
        return finder.apply(user, pageable);
    }

    @Transactional(readOnly = true)
    public boolean existsByPostsAndUser(Posts post, User user) {
        return likesRepository.existsByPostsAndUser(post, user);
    }

    @Transactional(readOnly = true)
    public boolean existsByCommentsAndUser(Comments comment, User user) {
        return likesRepository.existsByCommentsAndUser(comment, user);
    }

    private LikesSaveDto createLikesSaveDto(Long postsId, Long commentsId, User user) {
        Posts posts = postsId != null ? postsService.findById(postsId) : null;
        Comments comments = commentsId != null ? commentsService.findById(commentsId) : null;

        return LikesSaveDto.builder()
                .posts(posts).comments(comments).user(user).build();
    }

    private void deleteByPostId(Long postsId, User user) {
        Posts posts = postsService.findById(postsId);
        likesRepository.deleteByPosts(posts, user);
    }

    private void deleteByCommentId(Long commentsId, User user) {
        Comments comments = commentsService.findById(commentsId);
        likesRepository.deleteByComments(comments, user);
    }

    @Transactional
    private void delete(Long id) {
        Likes like = likesRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException(LIKE_NOT_FOUND.getMessage()));
        likesRepository.delete(like);
    }
}
