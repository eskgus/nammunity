package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Service
public class CommentsService {
    private final CommentsRepository commentsRepository;
    private final UserService userService;
    private final PostsSearchService postsSearchService;

    @Transactional
    public Long save(CommentsSaveDto requestDto, String username) {
        log.info("comments save in service.....");

        User user = userService.findByUsername(username);
        Posts posts = postsSearchService.findById(requestDto.getPostsId());

        CommentsSaveDto commentsSaveDto = CommentsSaveDto.builder()
                .content(requestDto.getContent())
                .posts(posts).user(user).build();

        return commentsRepository.save(commentsSaveDto.toEntity()).getId();
    }

    @Transactional(readOnly = true)
    public List<CommentsReadDto> findByPostsId(Posts posts) {
        return commentsRepository.findByPosts(posts).stream().map(CommentsReadDto::new)
                .collect(Collectors.toList());
    }
}
