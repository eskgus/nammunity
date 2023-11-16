package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LikesSearchService {

    @Transactional(readOnly = true)
    public List<LikesListDto> findLikesByUser(User user, Function<User, List<Likes>> function) {
        // function으로 들어온 findByUser(전체 좋아요), findPostsByUser(게시글 좋아요), findCommentsByUser(댓글 좋아요)에
        // user를 넣어 호출해서 얻은 List<Likes>를 List<LikesListDto>로 변환해서 리턴
        return function.apply(user).stream().map(LikesListDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countLikesByUser(User user, Function<User, Long> function) {
        // function으로 들어온 countByUser(전체 좋아요 개수), countPostLikesByUser(게시글 좋아요 개수),
        // countCommentLikesByUser(댓글 좋아요 개수)에 user를 넣어 호출해서 얻은 long을 리턴
        return function.apply(user);
    }
}
