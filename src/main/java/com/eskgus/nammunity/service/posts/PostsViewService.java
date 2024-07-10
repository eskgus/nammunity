package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsViewService;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.service.reports.ReasonsService;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostWithReasonsDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsReadDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import com.eskgus.nammunity.web.dto.reports.ReasonsListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PostsViewService {
    private final PostsService postsService;
    private final ReasonsService reasonsService;
    private final LikesService likesService;
    private final CommentsViewService commentsViewService;
    private final PrincipalHelper principalHelper;

    @Transactional
    public PostWithReasonsDto readPosts(Long postId, Principal principal) {
        PostsReadDto post = createPostsReadDto(postId, principal);
        List<ReasonsListDto> reasons = reasonsService.findAllAsc();
        return PostWithReasonsDto.builder().post(post).reasons(reasons).build();
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<CommentsReadDto> readComments(Long postId, Principal principal, int page) {
        Page<CommentsReadDto> commentsPage = createCommentsPage(postId, principal, page);
        return createContentsPageDto(commentsPage);
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<PostsListDto> listPosts(Principal principal, int page) {
        User user = getUserFromPrincipal(principal, true);
        Page<PostsListDto> postsPage = postsService.findByUser(user, page, 20);
        return createContentsPageDto(postsPage);
    }

    @Transactional(readOnly = true)
    public PostsUpdateDto updatePosts(Long id) {
        Posts post = findPostsById(id);
        return PostsUpdateDto.builder().id(id).title(post.getTitle()).content(post.getContent()).build();
    }

    private PostsReadDto createPostsReadDto(Long postId, Principal principal) {
        Posts post = findPostsById(postId);
        Long postAuthorId = post.getUser().getId();
        User user = getUserFromPrincipal(principal, false);
        boolean doesUserWritePost = doesUserWritePost(user, postAuthorId);

        if (!doesUserWritePost) {
            postsService.countView(post);
        }

        boolean doesUserLikePost = likesService.existsByPostsAndUser(post, user);

        return PostsReadDto.builder()
                .post(post).postedByUser(doesUserWritePost).likedByUser(doesUserLikePost).build();
    }

    private boolean doesUserWritePost(User user, Long postAuthorId) {
        if (user != null) {
            return postAuthorId.equals(user.getId());
        }
        return false;
    }

    private Page<CommentsReadDto> createCommentsPage(Long postId, Principal principal, int page) {
        Posts post = findPostsById(postId);
        User user = getUserFromPrincipal(principal, false);
        return commentsViewService.findCommentsPageByPosts(post, user, page);
    }

    private Posts findPostsById(Long postId) {
        return postsService.findById(postId);
    }

    private User getUserFromPrincipal(Principal principal, boolean throwExceptionOnMissingPrincipal) {
        return principalHelper.getUserFromPrincipal(principal, throwExceptionOnMissingPrincipal);
    }

    private <Dto> ContentsPageDto<Dto> createContentsPageDto(Page<Dto> contentsPage) {
        return new ContentsPageDto<>(contentsPage);
    }
}
