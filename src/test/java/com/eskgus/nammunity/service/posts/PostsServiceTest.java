package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.converter.PostsConverterForTest;
import com.eskgus.nammunity.domain.enums.SearchType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.ContentsPageDtoTestHelper;
import com.eskgus.nammunity.helper.PaginationTestHelper;
import com.eskgus.nammunity.helper.Range;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Principal;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PostsServiceTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private PostsService postsService;

    private User[] users;
    private Posts post;
    private final int page = 1;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        User user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDB.signUp(2L, Role.USER);
        User user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        this.users = new User[]{ user1, user2 };

        Long postId = testDB.savePosts(user1);
        this.post = assertOptionalAndGetEntity(postsRepository::findById, postId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDB.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void save() {
        User user = users[0];

        PostsSaveDto requestDto = PostsSaveDto.builder().title("title").content("content").build();
        Principal principal = user::getUsername;

        Long id = postsService.save(requestDto, principal);

        Posts post = assertOptionalAndGetEntity(postsRepository::findById, id);
        assertSavedPost(requestDto, user, post);
    }

    private void assertSavedPost(PostsSaveDto requestDto, User user, Posts post) {
        assertThat(post.getTitle()).isEqualTo(requestDto.getTitle());
        assertThat(post.getContent()).isEqualTo(requestDto.getContent());
        assertThat(post.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    public void findAllDesc() {
        savePosts();

        callAndAssertFindAllDesc();
    }

    private void savePosts() {
        int numberOfPostsByUser = 15;
        for (int i = 0; i < numberOfPostsByUser; i++) {
            for (User user : users) {
                testDB.savePosts(user);
            }
        }
        assertThat(postsRepository.count()).isEqualTo((long) numberOfPostsByUser * users.length + post.getId());
    }

    private void callAndAssertFindAllDesc() {
        ContentsPageDto<PostsListDto> actualResult = postsService.findAllDesc(page);
        Page<PostsListDto> expectedContents = createExpectedContents();

        ContentsPageDtoTestHelper<PostsListDto, Posts> findHelper = ContentsPageDtoTestHelper.<PostsListDto, Posts>builder()
                .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(new PostsConverterForTest()).build();
        findHelper.createExpectedResultAndAssertContentsPage();
    }

    private Page<PostsListDto> createExpectedContents() {
        Pageable pageable = createPageable(page, 20);
        return postsRepository.findAllDesc(pageable);
    }

    @Test
    public void findByUser() {
        savePosts();

        callAndAssertFindByUser();
    }

    private void callAndAssertFindByUser() {
        int size = 4;
        User user = users[0];

        Page<PostsListDto> actualContents = postsService.findByUser(user, page, size);
        Page<PostsListDto> expectedContents = createExpectedPage(size, user);

        assertContents(actualContents, expectedContents);
    }

    private Page<PostsListDto> createExpectedPage(int size, User user) {
        Pageable pageable = createPageable(page, size);
        return postsRepository.findByUser(user, pageable);
    }

    private void assertContents(Page<PostsListDto> actualContents, Page<PostsListDto> expectedContents) {
        PaginationTestHelper<PostsListDto, Posts> paginationHelper
                = new PaginationTestHelper<>(actualContents, expectedContents, new PostsConverterForTest());
        paginationHelper.assertContents();
    }

    @Test
    public void search() {
        savePostsWithTitleAndContent();

        // 1. searchBy = title (title)
        callAndAssertSearch("title", SearchType.TITLE);

        // 2. searchBy = content (content)
        callAndAssertSearch("content", SearchType.CONTENT);

        // 3. searchBy = title and content (제목 내용)
        callAndAssertSearch("제목 내용", SearchType.TITLE_AND_CONTENT);
    }

    private void savePostsWithTitleAndContent() {
        long numberOfPosts = 20;
        long half = numberOfPosts / 2;

        Range firstRange = Range.builder()
                .startIndex(1).endIndex(half)
                .title("title").content("content").build();
        Range secondRange = Range.builder()
                .startIndex(half + 1).endIndex(numberOfPosts)
                .title("제목").content("내용").build();

        savePostsInRange(firstRange);
        savePostsInRange(secondRange);
    }

    private void savePostsInRange(Range range) {
        for (long i = range.getStartIndex(); i <= range.getEndIndex(); i++) {
            Long postId = testDB.savePostWithTitleAndContent(users[0], range.getTitle() + i, range.getContent() + i);
            assertOptionalAndGetEntity(postsRepository::findById, postId);
        }
    }

    private void callAndAssertSearch(String keywords, SearchType searchType) {
        String searchBy = searchType.getKey();
        int size = 3;

        Page<PostsListDto> actualContents = postsService.search(keywords, searchBy, page, size);
        Page<PostsListDto> expectedContents = createExpectedPage(keywords, searchType, size);

        assertContents(actualContents, expectedContents);
    }

    private Page<PostsListDto> createExpectedPage(String keywords, SearchType searchType, int size) {
        Pageable pageable = createPageable(page, size);
        BiFunction<String, Pageable, Page<PostsListDto>> searcher = getSearcher(searchType);
        return searcher.apply(keywords, pageable);
    }

    private BiFunction<String, Pageable, Page<PostsListDto>> getSearcher(SearchType searchType) {
        if (searchType.equals(SearchType.TITLE)) {
            return postsRepository::searchByTitle;
        } else if (searchType.equals(SearchType.CONTENT)) {
            return postsRepository::searchByContent;
        }
        return postsRepository::searchByTitleAndContent;
    }
}
