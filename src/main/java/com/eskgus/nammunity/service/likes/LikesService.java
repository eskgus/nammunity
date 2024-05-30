package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import com.eskgus.nammunity.web.dto.likes.LikesSaveDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.function.BiFunction;

@RequiredArgsConstructor
@Service
public class LikesService {
    private final PostsService postsService;
    private final CommentsSearchService commentsSearchService;
    private final LikesSearchService likesSearchService;
    private final LikesRepository likesRepository;

    @Autowired
    private PrincipalHelper principalHelper;

    @Transactional
    public Long save(Long postsId, Long commentsId, Principal principal) {
        User user = principalHelper.getUserFromPrincipal(principal, true);

        Posts posts = null;
        Comments comments = null;
        if (postsId != null) {
            posts = postsService.findById(postsId);
        } else {
            comments = commentsSearchService.findById(commentsId);
        }

        LikesSaveDto likesSaveDto = LikesSaveDto.builder()
                .posts(posts).comments(comments).user(user).build();

        return likesRepository.save(likesSaveDto.toEntity()).getId();
    }

    @Transactional
    public void deleteByContentId(Long postsId, Long commentsId, Principal principal) {
        User user = principalHelper.getUserFromPrincipal(principal, true);

        if (postsId != null) {
            Posts posts = postsService.findById(postsId);
            likesRepository.deleteByPosts(posts, user);
        } else {
            Comments comments = commentsSearchService.findById(commentsId);
            likesRepository.deleteByComments(comments, user);
        }
    }

    @Transactional
    public void deleteSelectedLikes(List<Long> likeIds) {
        if (likeIds.isEmpty()) {
            throw new IllegalArgumentException("삭제할 항목을 선택하세요.");
        }

        likeIds.forEach(this::delete);
    }

    @Transactional
    private void delete(Long id) {
        Likes like = likesRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 좋아요가 없습니다."));
        likesRepository.delete(like);
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<LikesListDto> listLikes(BiFunction<User, Pageable, Page<LikesListDto>> finder,
                                                   Principal principal, int page) {
        User user = principalHelper.getUserFromPrincipal(principal, true);
        Page<LikesListDto> contents = likesSearchService.findLikesByUser(user, finder, page, 20);
        return new ContentsPageDto<>(contents);
    }
}
