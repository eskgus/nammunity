package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.enums.SearchType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.function.BiFunction;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;

@RequiredArgsConstructor
@Service
public class PostsService {
    private final PostsRepository postsRepository;
    private final PrincipalHelper principalHelper;

    @Transactional
    public Long save(PostsSaveDto requestDto, Principal principal) {
        User user = principalHelper.getUserFromPrincipal(principal, true);
        PostsSaveDto postsSaveDto = PostsSaveDto.builder()
                .title(requestDto.getTitle()).content(requestDto.getContent())
                .user(user).build();
        return postsRepository.save(postsSaveDto.toEntity()).getId();
    }

    @Transactional
    public Long update(Long id, PostsUpdateDto requestDto) {
        Posts posts = findById(id);
        posts.update(requestDto.getTitle(), requestDto.getContent());
        return id;
    }

    @Transactional
    public void delete(Long id) {
        Posts posts = findById(id);
        postsRepository.delete(posts);
    }

    @Transactional
    public void deleteSelectedPosts(List<Long> postIds) {
        if (postIds.isEmpty()) {
            throw new IllegalArgumentException("삭제할 항목을 선택하세요.");
        }

        postIds.forEach(this::delete);
    }

    @Transactional
    public void countView(Posts post) {
        post.countView();
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<PostsListDto> findAllDesc(int page) {
        Pageable pageable = createPageable(page, 20);
        Page<PostsListDto> contents = postsRepository.findAllDesc(pageable);
        return new ContentsPageDto<>(contents);
    }

    @Transactional(readOnly = true)
    public Posts findById(Long id) {
        return postsRepository.findById(id).orElseThrow(() -> new
                IllegalArgumentException("해당 게시글이 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<PostsListDto> findByUser(User user, int page, int size) {
        Pageable pageable = createPageable(page, size);
        return postsRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public long countByUser(User user) {
        return postsRepository.countByUser(user);
    }

    @Transactional(readOnly = true)
    public Page<PostsListDto> search(String keywords, String searchBy, int page, int size) {
        Pageable pageable = createPageable(page, size);

        SearchType searchType = SearchType.convertSearchBy(searchBy);
        BiFunction<String, Pageable, Page<PostsListDto>> searcher = getSearcherBySearchType(searchType);

        return searcher.apply(keywords, pageable);
    }

    private BiFunction<String, Pageable, Page<PostsListDto>> getSearcherBySearchType(SearchType searchType) {
        return switch (searchType) {
            case TITLE -> postsRepository::searchByTitle;
            case CONTENT -> postsRepository::searchByContent;
            default -> postsRepository::searchByTitleAndContent;
        };
    }
}
