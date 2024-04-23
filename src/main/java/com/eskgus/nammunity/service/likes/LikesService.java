package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
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
    private final UserService userService;
    private final PostsSearchService postsSearchService;
    private final CommentsSearchService commentsSearchService;
    private final LikesSearchService likesSearchService;
    private final LikesRepository likesRepository;

    @Autowired
    private PrincipalHelper principalHelper;

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

    @Transactional
    public void deleteSelectedLikes(List<Long> likesId) {
        if (likesId.isEmpty()) {
            throw new IllegalArgumentException("삭제할 항목을 선택하세요.");
        }

        likesId.forEach(likesRepository::deleteById);
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<LikesListDto> listLikes(BiFunction<User, Pageable, Page<LikesListDto>> finder,
                                                   Principal principal, int page) {
        User user = principalHelper.getUserFromPrincipal(principal);
        Page<LikesListDto> contents = likesSearchService.findLikesByUser(user, finder, page, 20);
        return new ContentsPageDto<>(contents);
    }
}
