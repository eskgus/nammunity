package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.likes.LikesSearchService;
import com.eskgus.nammunity.service.reports.ReasonsService;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostWithReasonsDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsReadDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import com.eskgus.nammunity.web.dto.reports.ReasonsListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final LikesSearchService likesSearchService;
    private final CommentsSearchService commentsSearchService;

    @Autowired
    private PrincipalHelper principalHelper;

    @Transactional
    public PostWithReasonsDto readPosts(Long postId, Principal principal) {
        PostsReadDto post = createPostsReadDto(postId, principal);
        List<ReasonsListDto> reasons = reasonsService.findAllAsc();
        return PostWithReasonsDto.builder().post(post).reasons(reasons).build();
    }

    private PostsReadDto createPostsReadDto(Long postId, Principal principal) {
        Posts post = postsService.findById(postId);
        Long postAuthorId = post.getUser().getId();
        User user = principalHelper.getUserFromPrincipal(principal, false);
        boolean doesUserWritePost = doesUserWritePost(user, postAuthorId);

        if (!doesUserWritePost) {
            postsService.countView(post);
        }

        boolean doesUserLikePost = likesSearchService.existsByPostsAndUser(post, user);

        return PostsReadDto.builder()
                .post(post).doesUserWritePost(doesUserWritePost).doesUserLikePost(doesUserLikePost).build();
    }

    public boolean doesUserWritePost(User user, Long postAuthorId) {
        if (user != null) {
            return postAuthorId.equals(user.getId());
        }
        return false;
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<CommentsReadDto> readComments(Long postId, Principal principal, int page) {
        Page<CommentsReadDto> contents = createCommentsPage(postId, principal, page);
        return new ContentsPageDto<>(contents);
    }

    private Page<CommentsReadDto> createCommentsPage(Long postId, Principal principal, int page) {
        Posts post = postsService.findById(postId);
        User user = principalHelper.getUserFromPrincipal(principal, false);
        return commentsSearchService.findByPosts(post, user, page);
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<PostsListDto> listPosts(Principal principal, int page) {
        User user = principalHelper.getUserFromPrincipal(principal, true);
        Page<PostsListDto> contents = postsService.findByUser(user, page, 20);
        return new ContentsPageDto<>(contents);
    }

    @Transactional(readOnly = true)
    public PostsUpdateDto updatePosts(Long id) {
        Posts post = postsService.findById(id);
        return PostsUpdateDto.builder().id(id).title(post.getTitle()).content(post.getContent()).build();
    }
}
