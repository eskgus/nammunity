package com.eskgus.nammunity.domain.posts;

import com.eskgus.nammunity.config.TestConfig;
import com.eskgus.nammunity.converter.PostsConverterForTest;
import com.eskgus.nammunity.helper.*;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;
import static com.eskgus.nammunity.util.PaginationTestUtil.createPageWithContent;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({ TestDataHelper.class, TestConfig.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PostsRepositoryTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    private User[] users;
    private final int page = 1;
    private final PostsConverterForTest entityConverter = new PostsConverterForTest();

    @BeforeEach
    public void setUp() {
        Long user1Id = testDataHelper.signUp(1L, Role.USER);
        User user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDataHelper.signUp(2L, Role.USER);
        User user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        this.users = new User[]{ user1, user2 };
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void save() {
        Posts post = createPost();
        callAndAssertSavePosts(post);
    }

    private Posts createPost() {
        return Posts.builder().title("title").content("content").user(users[0]).build();
    }

    private void callAndAssertSavePosts(Posts post) {
        postsRepository.save(post);
        Posts actualPost = assertOptionalAndGetEntity(postsRepository::findById, post.getId());
        assertActualPostEqualsExpectedPost(actualPost, post);
    }

    private void assertActualPostEqualsExpectedPost(Posts actualPost, Posts expectedPost) {
        assertThat(actualPost.getTitle()).isEqualTo(expectedPost.getTitle());
        assertThat(actualPost.getContent()).isEqualTo(expectedPost.getContent());
    }

    @Test
    public void addBaseTimeEntity() {
        Posts savedPost = savePostAndGetSavedPost();

        LocalDateTime now = LocalDateTime.now();

        assertThat(savedPost.getCreatedDate()).isBefore(now);
        assertThat(savedPost.getModifiedDate()).isBefore(now);
    }

    private Posts savePostAndGetSavedPost() {
        Long postId = testDataHelper.savePosts(users[0]);
        return assertOptionalAndGetEntity(postsRepository::findById, postId);
    }

    @Test
    public void countByUser() {
        // 1. 게시글 작성 x 후 호출
        callAndAssertCountByUser(0L);

        // 2. 게시글 1개 작성 후 호출
        Posts post = savePostAndGetSavedPost();
        callAndAssertCountByUser(post.getId());
    }

    private void callAndAssertCountByUser(Long expectedCount) {
        Long actualCount = postsRepository.countByUser(users[0]);
        assertThat(actualCount).isEqualTo(expectedCount);
    }

    @Test
    public void searchByTitle() {
        savePosts();

        // 1. 검색 제외 단어 x
        callAndAssertSearch(postsRepository::searchByTitle, "ti 제목", Posts::getTitle);

        // 2. 검색 제외 단어 o
        callAndAssertSearch(postsRepository::searchByTitle, "ti 제목 -tle", Posts::getTitle);
    }

    private void savePosts() {
        long numberOfPostsByUser = 10;
        long half = numberOfPostsByUser / 2;

        Range firstRange = Range.builder().startIndex(1).endIndex(half).title("title").content("content").build();
        Range secondRange = Range.builder().startIndex(half + 1).endIndex(numberOfPostsByUser)
                .title("제목").content("내용").build();

        savePostsInRange(firstRange);
        savePostsInRange(secondRange);
    }

    private void savePostsInRange(Range range) {
        for (User user : users) {
            for (long i = range.getStartIndex(); i <= range.getEndIndex(); i++) {
                Long postId = testDataHelper.savePostWithTitleAndContent(user, range.getTitle() + i, range.getContent() + i);
                assertOptionalAndGetEntity(postsRepository::findById, postId);
            }
        }
    }

    private void callAndAssertSearch(BiFunction<String, Pageable, Page<PostsListDto>> searcher, String keywords,
                                     Function<Posts, String>... fieldExtractors) {
        int size = 3;

        Pageable pageable = createPageable(page, size);

        Page<PostsListDto> actualContents = searcher.apply(keywords, pageable);
        Page<PostsListDto> expectedContents = createExpectedContents(keywords, pageable, fieldExtractors);

        assertContents(actualContents, expectedContents);
    }

    private Page<PostsListDto> createExpectedContents(String keywords, Pageable pageable,
                                                      Function<Posts, String>... fieldExtractors) {
        SearchTestHelper<Posts> searchHelper = SearchTestHelper.<Posts>builder()
                .totalContents(postsRepository.findAll()).keywords(keywords)
                .fieldExtractors(fieldExtractors).build();
        Stream<Posts> filteredPostsStream = searchHelper.getKeywordsFilter();

        return createPageWithContent(filteredPostsStream, entityConverter, pageable);
    }

    private void assertContents(Page<PostsListDto> actualContents, Page<PostsListDto> expectedContents) {
        PaginationTestHelper<PostsListDto, Posts> paginationHelper
                = new PaginationTestHelper<>(actualContents, expectedContents, entityConverter);
        paginationHelper.assertContents();
    }

    @Test
    public void searchByContent() {
        savePosts();

        // 1. 검색 제외 단어 x
        callAndAssertSearch(postsRepository::searchByContent, "con 내용", Posts::getContent);

        // 2. 검색 제외 단어 o
        callAndAssertSearch(postsRepository::searchByContent, "con 내용 -용", Posts::getContent);
    }

    @Test
    public void searchByTitleAndContent() {
        savePosts();

        // 1. 검색 제외 단어 x
        callAndAssertSearch(postsRepository::searchByTitleAndContent, "title 내용",
                Posts::getTitle, Posts::getContent);

        // 2. 검색 제외 단어 o
        callAndAssertSearch(postsRepository::searchByTitleAndContent, "title 내용 -제목",
                Posts::getTitle, Posts::getContent);
    }

    @Test
    public void findAllDesc() {
        savePosts();

        callAndAssertFindAllDesc();
    }

    private void callAndAssertFindAllDesc() {
        int size = 4;

        Pageable pageable = createPageable(page, size);

        Page<PostsListDto> actualContents = postsRepository.findAllDesc(pageable);
        Page<PostsListDto> expectedContents = createExpectedContents(null, pageable);

        assertContents(actualContents, expectedContents);
    }

    private Page<PostsListDto> createExpectedContents(Predicate<Posts> filter, Pageable pageable) {
        Stream<Posts> filteredPostsStream = filter != null
                ? postsRepository.findAll().stream().filter(filter) : postsRepository.findAll().stream();
        return createPageWithContent(filteredPostsStream, entityConverter, pageable);
    }

    @Test
    public void findByUser() {
        savePosts();

        callAndAssertFindByUser();
    }

    private void callAndAssertFindByUser() {
        int size = 4;
        User user = users[0];

        Pageable pageable = createPageable(page, size);

        Page<PostsListDto> actualContents = postsRepository.findByUser(user, pageable);
        Page<PostsListDto> expectedContents = createExpectedContentsByUser(user, pageable);

        assertContents(actualContents, expectedContents);
    }

    private Page<PostsListDto> createExpectedContentsByUser(User user, Pageable pageable) {
        Predicate<Posts> filter = post -> entityConverter.extractUserId(post).equals(user.getId());
        return createExpectedContents(filter, pageable);
    }
}
