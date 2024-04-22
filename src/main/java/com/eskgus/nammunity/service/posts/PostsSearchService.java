package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.enums.SearchType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.BiFunction;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;

@RequiredArgsConstructor
@Service
public class PostsSearchService {
    private final PostsRepository postsRepository;

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
        Pageable pageable = PageRequest.of(page - 1, size);

        SearchType searchType = SearchType.convertSearchBy(searchBy);
        BiFunction<String, Pageable, Page<PostsListDto>> searcher = getSearcherBySearchType(searchType);

        return searcher.apply(keywords, pageable);
    }

    private BiFunction<String, Pageable, Page<PostsListDto>> getSearcherBySearchType(SearchType searchType) {
        if (searchType.equals(SearchType.TITLE)) {
            return postsRepository::searchByTitle;
        } else if (searchType.equals(SearchType.CONTENT)) {
            return postsRepository::searchByContent;
        }
        return postsRepository::searchByTitleAndContent;
    }
}
