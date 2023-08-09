package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.likes.LikesSearchService;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Service
public class CommentsSearchService {
    private final CommentsRepository commentsRepository;
    private final LikesSearchService likesSearchService;

    @Transactional(readOnly = true)
    public List<CommentsReadDto> findByPosts(Posts posts, User user) {
        return commentsRepository.findByPosts(posts).stream().map(comments -> {
                    List<LikesListDto> likes = likesSearchService.findByComments(comments);
                    return CommentsReadDto.builder().comments(comments).user(user).likes(likes).build();
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Comments findById(Long id) {
        return commentsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 댓글이 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<CommentsListDto> findByUser(User user) {
        return commentsRepository.findByUser(user).stream().map(comments -> {
                    int likes = likesSearchService.countByComments(comments);
                    return new CommentsListDto(comments, likes);
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public int countByPosts(Posts posts) {
        return commentsRepository.countByPosts(posts);
    }
}
