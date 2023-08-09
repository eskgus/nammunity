package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LikesSearchService {
    private final LikesRepository likesRepository;

    @Transactional(readOnly = true)
    public List<LikesListDto> findByPosts(Posts posts) {
        return likesRepository.findByPosts(posts).stream().map(LikesListDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LikesListDto> findByUser(User user) {
        return likesRepository.findByUser(user).stream().map(LikesListDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LikesListDto> findByComments(Comments comments) {
        return likesRepository.findByComments(comments).stream().map(LikesListDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LikesListDto> findPostsByUser(User user) {
        return likesRepository.findPostsByUser(user).stream().map(LikesListDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LikesListDto> findCommentsByUser(User user) {
        return likesRepository.findCommentsByUser(user).stream().map(LikesListDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public int countByPosts(Posts posts) {
        return likesRepository.countByPosts(posts);
    }

    @Transactional(readOnly = true)
    public int countByComments(Comments comments) {
        return likesRepository.countByComments(comments);
    }
}
