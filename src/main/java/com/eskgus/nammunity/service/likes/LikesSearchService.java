package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.BiFunction;

@RequiredArgsConstructor
@Service
public class LikesSearchService {
    private final LikesRepository likesRepository;

    @Transactional(readOnly = true)
    public Page<LikesListDto> findLikesByUser(User user, BiFunction<User, Pageable, Page<LikesListDto>> finder,
                                              int page, int size) {
        // finder: likesRepository.findByUser(전체 좋아요), findPostLikesByUser(게시글 좋아요), findCommentLikesByUser(댓글 좋아요)
        Pageable pageable = PageRequest.of(page - 1, size);
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
}
