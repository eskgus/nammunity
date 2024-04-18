package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.likes.LikesSearchService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;

@RequiredArgsConstructor
@Service
public class CommentsSearchService {
    private final CommentsRepository commentsRepository;
    private final LikesSearchService likesSearchService;

    @Transactional(readOnly = true)
    public Page<CommentsReadDto> findByPosts(Posts post, User user, int page) {
        Pageable pageable = createPageable(page, 30);
        Page<CommentsReadDto> comments = commentsRepository.findByPosts(post, pageable);
        setCommentsReadDto(comments.getContent(), user);
        return comments;
    }

    private void setCommentsReadDto(List<CommentsReadDto> comments, User user) {
        comments.forEach(commentsReadDto -> {
            boolean doesUserWriteComment = user == null ? false : commentsReadDto.getAuthorId().equals(user.getId());
            commentsReadDto.setDoesUserWriteComment(doesUserWriteComment);

            Comments comment = findById(commentsReadDto.getId());
            boolean doesUserLikeComment = likesSearchService.existsByCommentsAndUser(comment, user);
            commentsReadDto.setDoesUserLikeComment(doesUserLikeComment);
        });
    }

    @Transactional(readOnly = true)
    public Comments findById(Long id) {
        return commentsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 댓글이 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<CommentsListDto> findByUser(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return commentsRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public long countByUser(User user) {
        return commentsRepository.countByUser(user);
    }

    @Transactional(readOnly = true)
    public Page<CommentsListDto> searchByContent(String keywords, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return commentsRepository.searchByContent(keywords, pageable);
    }
}
