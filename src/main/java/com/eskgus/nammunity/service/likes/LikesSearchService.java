package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.BiFunction;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class LikesSearchService {
    @Transactional(readOnly = true)
    public Page<LikesListDto> findLikesByUser(User user, BiFunction<User, Pageable, Page<LikesListDto>> finder,
                                              int page, int size) {
        // finder: likesRepository.findByUser(전체 좋아요), findPostLikesByUser(게시글 좋아요), findCommentLikesByUser(댓글 좋아요)
        Pageable pageable = PageRequest.of(page - 1, size);
        return finder.apply(user, pageable);
    }

    @Transactional(readOnly = true)
    public long countLikesByUser(User user, Function<User, Long> function) {
        // function으로 들어온 countByUser(전체 좋아요 개수), countPostLikesByUser(게시글 좋아요 개수),
        // countCommentLikesByUser(댓글 좋아요 개수)에 user를 넣어 호출해서 얻은 long을 리턴
        return function.apply(user);
    }
}
