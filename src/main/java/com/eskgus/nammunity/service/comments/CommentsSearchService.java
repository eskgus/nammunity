package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentsSearchService {
    private final CommentsRepository commentsRepository;

    @Transactional(readOnly = true)
    public List<CommentsReadDto> findByPosts(Posts posts, User user) {
        return posts.getComments().stream().map(comments -> new CommentsReadDto(comments, user))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Comments findById(Long id) {
        return commentsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 댓글이 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<CommentsListDto> findByUser(User user) {
        return commentsRepository.findByUser(user).stream().map(CommentsListDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countByUser(User user) {
        return commentsRepository.countByUser(user);
    }

    @Transactional(readOnly = true)
    public List<CommentsListDto> searchByContent(String keywords) {
        return commentsRepository.searchByContent(keywords).stream().map(CommentsListDto::new).toList();
    }
}
