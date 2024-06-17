package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.service.likes.LikesService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentsViewService {
    private final CommentsService commentsService;
    private final LikesService likesService;
    private final PrincipalHelper principalHelper;

    @Transactional(readOnly = true)
    public Page<CommentsReadDto> findCommentsPageByPosts(Posts post, User user, int page) {
        Page<CommentsReadDto> comments = commentsService.findByPosts(post, page);
        setCommentsReadDto(comments.getContent(), user);
        return comments;
    }

    private void setCommentsReadDto(List<CommentsReadDto> comments, User user) {
        comments.forEach(commentsReadDto -> {
            boolean doesUserWriteComment = user == null ? false : commentsReadDto.getAuthorId().equals(user.getId());
            commentsReadDto.setDoesUserWriteComment(doesUserWriteComment);

            Comments comment = commentsService.findById(commentsReadDto.getId());
            boolean doesUserLikeComment = likesService.existsByCommentsAndUser(comment, user);
            commentsReadDto.setDoesUserLikeComment(doesUserLikeComment);
        });
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<CommentsListDto> listComments(Principal principal, int page) {
        User user = principalHelper.getUserFromPrincipal(principal, true);
        Page<CommentsListDto> contents = commentsService.findByUser(user, page, 20);
        return new ContentsPageDto<>(contents);
    }
}
