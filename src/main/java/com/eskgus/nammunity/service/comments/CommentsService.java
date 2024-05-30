package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentsService {
    private final CommentsRepository commentsRepository;
    private final PostsService postsService;
    private final CommentsSearchService commentsSearchService;

    @Autowired
    private PrincipalHelper principalHelper;

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
        Comments comments = commentsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 댓글이 없습니다."));
        comments.update(content);
        return id;
    }

    @Transactional
    public void delete(Long id) {
        Comments comments = commentsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 댓글이 없습니다."));
        commentsRepository.delete(comments);
    }

    @Transactional
    public void deleteSelectedComments(List<Long> commentIds) {
        if (commentIds.isEmpty()) {
            throw new IllegalArgumentException("삭제할 항목을 선택하세요.");
        }

        commentIds.forEach(this::delete);
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<CommentsListDto> listComments(Principal principal, int page) {
        User user = principalHelper.getUserFromPrincipal(principal, true);
        Page<CommentsListDto> contents = commentsSearchService.findByUser(user, page, 20);
        return new ContentsPageDto<>(contents);
    }
}
