package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.EMPTY_CONTENT_IDS;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.NON_EXISTENT_COMMENT;
import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;

@RequiredArgsConstructor
@Service
public class CommentsService {
    private final CommentsRepository commentsRepository;
    private final PostsService postsService;
    private final PrincipalHelper principalHelper;

    @Transactional
    public Long save(CommentsSaveDto requestDto, Principal principal) {
        User user = principalHelper.getUserFromPrincipal(principal, true);
        Posts post = postsService.findById(requestDto.getPostsId());

        CommentsSaveDto commentsSaveDto = CommentsSaveDto.builder()
                .content(requestDto.getContent()).posts(post).user(user).build();

        return commentsRepository.save(commentsSaveDto.toEntity()).getId();
    }

    @Transactional
    public Long update(Long id, String content) {
        Comments comments = findById(id);
        comments.update(content);
        return id;
    }

    @Transactional
    public void deleteSelectedComments(List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            throw new IllegalArgumentException(EMPTY_CONTENT_IDS.getMessage());
        }

        commentIds.forEach(this::delete);
    }

    @Transactional
    public void delete(Long id) {
        Comments comments = findById(id);
        commentsRepository.delete(comments);
    }

    @Transactional(readOnly = true)
    public Comments findById(Long id) {
        return commentsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException(NON_EXISTENT_COMMENT.getMessage()));
    }

    @Transactional(readOnly = true)
    public Page<CommentsReadDto> findByPosts(Posts post, int page) {
        Pageable pageable = createPageable(page, 30);
        return commentsRepository.findByPosts(post, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CommentsListDto> findByUser(User user, int page, int size) {
        Pageable pageable = createPageable(page, size);
        return commentsRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public long countByUser(User user) {
        return commentsRepository.countByUser(user);
    }

    @Transactional(readOnly = true)
    public Page<CommentsListDto> searchByContent(String keywords, int page, int size) {
        Pageable pageable = createPageable(page, size);
        return commentsRepository.searchByContent(keywords, pageable);
    }

    @Transactional(readOnly = true)
    public int calculateCommentPage(Long postId, Long commentId) {
        long commentIndex = commentsRepository.countCommentIndex(postId, commentId);
        return (int) commentIndex / 30 + 1;
    }
}
