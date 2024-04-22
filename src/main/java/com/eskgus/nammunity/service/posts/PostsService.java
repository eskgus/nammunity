package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.likes.LikesSearchService;
import com.eskgus.nammunity.service.reports.ReasonsService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.*;
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
public class PostsService {
    private final PostsRepository postsRepository;
    private final UserService userService;
    private final CommentsSearchService commentsSearchService;
    private final ReasonsService reasonsService;
    private final LikesSearchService likesSearchService;
    private final PostsSearchService postsSearchService;

    @Autowired
    private PrincipalHelper principalHelper;

    @Transactional
    public Long save(PostsSaveDto requestDto, Long id) {
        User user = userService.findById(id);
        PostsSaveDto postsSaveDto = PostsSaveDto.builder()
                .title(requestDto.getTitle()).content(requestDto.getContent())
                .user(user).build();
        return postsRepository.save(postsSaveDto.toEntity()).getId();
    }

    @Transactional
    public Long update(Long id, PostsUpdateDto requestDto) {
        Posts posts = postsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 게시글이 없습니다."));
        posts.update(requestDto.getTitle(), requestDto.getContent());
        return id;
    }

    @Transactional
    public void delete(Long id) {
        Posts posts = postsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 게시글이 없습니다."));
        postsRepository.delete(posts);
    }

    @Transactional
    public void deleteSelectedPosts(List<Long> postsId) {
        if (postsId.isEmpty()) {
            throw new IllegalArgumentException("삭제할 항목을 선택하세요.");
        }

        postsId.forEach(this::delete);
    }

    @Transactional
    public PostWithReasonsDto readPosts(Long postId, Principal principal) {
        PostsReadDto post = createPostsReadDto(postId, principal);
        List<ReasonsListDto> reasons = reasonsService.findAllAsc();
        return PostWithReasonsDto.builder().post(post).reasons(reasons).build();
    }

    private PostsReadDto createPostsReadDto(Long postId, Principal principal) {
        Posts post = postsSearchService.findById(postId);
        Long postAuthorId = post.getUser().getId();
        User user = principalHelper.getUserFromPrincipal(principal);
        boolean doesUserWritePost = doesUserWritePost(user, postAuthorId);

        if (!doesUserWritePost) {
            countView(post);
        }

        boolean doesUserLikePost = likesSearchService.existsByPostsAndUser(post, user);

        return PostsReadDto.builder()
                .post(post).doesUserWritePost(doesUserWritePost).doesUserLikePost(doesUserLikePost).build();
    }

    private boolean doesUserWritePost(User user, Long postAuthorId) {
        if (user != null) {
            return postAuthorId.equals(user.getId());
        }
        return false;
    }

    private void countView(Posts post) {
        post.countView();
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<CommentsReadDto> readComments(Long postId, Principal principal, int page) {
        Page<CommentsReadDto> contents = createCommentsPage(postId, principal, page);
        return new ContentsPageDto<>(contents);
    }

    private Page<CommentsReadDto> createCommentsPage(Long postId, Principal principal, int page) {
        Posts post = postsSearchService.findById(postId);
        User user = principalHelper.getUserFromPrincipal(principal);
        return commentsSearchService.findByPosts(post, user, page);
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<PostsListDto> listPosts(Principal principal, int page) {
        User user = principalHelper.getUserFromPrincipal(principal);
        Page<PostsListDto> contents = postsSearchService.findByUser(user, page, 20);
        return new ContentsPageDto<>(contents);
    }
}
