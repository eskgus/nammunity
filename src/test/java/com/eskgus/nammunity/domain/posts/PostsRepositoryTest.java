package com.eskgus.nammunity.domain.posts;

import com.eskgus.nammunity.config.TestConfig;
import com.eskgus.nammunity.converter.PostsConverterForTest;
import com.eskgus.nammunity.helper.*;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.util.PaginationRepoUtil;
import com.eskgus.nammunity.util.PaginationTestUtil;
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

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    private static final int PAGE = 1;
    private static final PostsConverterForTest POSTS_CONVERTER = new PostsConverterForTest();

    @BeforeEach
    public void setUp() {
        Long user1Id = testDataHelper.signUp(1L, Role.USER);
        User user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDataHelper.signUp(2L, Role.USER);
        User user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        this.users = new User[]{ user1, user2 };
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void countPostsByUserWithoutPosts() {
        // given
        // when/then
        testCountPostsByUser(users[0], 0L);
    }

    @Test
    public void countPostsByUserWithPosts() {
        // given
        long numberOfPosts = savePosts();

        // when/then
        testCountPostsByUser(users[0], numberOfPosts);
    }

    @Test
    public void searchPostsByTitleWithoutExcludeKeywords() {
        testSearchPostsByTitle("ti 제");
    }

    @Test
    public void searchPostsByTitleWithExcludeKeywords() {
        testSearchPostsByTitle("ti 제 -목");
    }

    @Test
    public void searchPostsByContentWithoutExcludeKeywords() {
        testSearchPostsByContent("con 내");
    }

    @Test
    public void searchPostsByContentWithExcludeKeywords() {
        testSearchPostsByContent("con 내 -용");
    }

    @Test
    public void searchPostsByTitleAndContentWithoutExcludeKeywords() {
        testSearchPostsByTitleAndContent("ti 내");
    }

    @Test
    public void searchPostsByTitleAndContentWithExcludeKeywords() {
        testSearchPostsByTitleAndContent("ti con 내 -용");
    }

    @Test
    public void findAllPostsDesc() {
        // given
        savePosts();

        Pageable pageable = createPageable();

        Page<PostsListDto> postsPage = createPostsPageWithFilter(null, pageable);

        // when
        Page<PostsListDto> result = postsRepository.findAllDesc(pageable);

        // then
        assertPostsPage(result, postsPage);
    }

    @Test
    public void findPostsByUser() {
        // given
        savePostsWithTitleAndContent();

        User user = users[0];

        Pageable pageable = createPageable();

        Predicate<Posts> filter = createFilter(user);
        Page<PostsListDto> postsPage = createPostsPageWithFilter(filter, pageable);

        // when
        Page<PostsListDto> result = postsRepository.findByUser(user, pageable);

        // then
        assertPostsPage(result, postsPage);
    }

    private void testCountPostsByUser(User user, long numberOfPosts) {
        // when
        long result = postsRepository.countByUser(user);

        // then
        assertEquals(numberOfPosts, result);
    }

    private void testSearchPostsByTitle(String keywords) {
        testSearchPostsBySearchType(keywords, postsRepository::searchByTitle, Posts::getTitle);
    }

    private void testSearchPostsByContent(String keywords) {
        testSearchPostsBySearchType(keywords, postsRepository::searchByContent, Posts::getContent);
    }

    private void testSearchPostsByTitleAndContent(String keywords) {
        testSearchPostsBySearchType(
                keywords, postsRepository::searchByTitleAndContent, Posts::getTitle, Posts::getContent);
    }

    private void testSearchPostsBySearchType(String keywords, BiFunction<String, Pageable, Page<PostsListDto>> searcher,
                                             Function<Posts, String>... fieldExtractors) {
        // given
        savePostsWithTitleAndContent();

        Pageable pageable = createPageable();

        SearchTestHelper<Posts> searchHelper = createSearchHelper(keywords, fieldExtractors);
        Page<PostsListDto> postsPage = createPostsPage(searchHelper, pageable);

        // when
        Page<PostsListDto> result = searcher.apply(keywords, pageable);

        // then
        assertPostsPage(result, postsPage);
    }

    private long savePosts() {
        long numberOfPosts = 3L;

        for (long i = 0; i < numberOfPosts; i++) {
            Long postId = testDataHelper.savePosts(users[0]);
            assertOptionalAndGetEntity(postsRepository::findById, postId);
        }

        return numberOfPosts;
    }

    private void savePostsWithTitleAndContent() {
        long numberOfPostsByUser = 10;
        long half = numberOfPostsByUser / 2;

        Range firstRange = Range.builder().startIndex(1).endIndex(half).title("title").content("content").build();
        Range secondRange = Range.builder()
                .startIndex(half + 1).endIndex(numberOfPostsByUser).title("제목").content("내용").build();

        savePostsInRange(firstRange);
        savePostsInRange(secondRange);
    }

    private void savePostsInRange(Range range) {
        for (User user : users) {
            for (long i = range.getStartIndex(); i <= range.getEndIndex(); i++) {
                Long postId = testDataHelper.savePostWithTitleAndContent(user, range.getTitle(), range.getContent());
                assertOptionalAndGetEntity(postsRepository::findById, postId);
            }
        }
    }

    private Pageable createPageable() {
        return PaginationRepoUtil.createPageable(PAGE, 3);
    }

    private SearchTestHelper<Posts> createSearchHelper(String keywords, Function<Posts, String>... fieldExtractors) {
        return SearchTestHelper.<Posts>builder()
                .totalContents(postsRepository.findAll()).keywords(keywords).fieldExtractors(fieldExtractors).build();
    }

    private Predicate<Posts> createFilter(User user) {
        return post -> POSTS_CONVERTER.extractUserId(post).equals(user.getId());
    }

    private Page<PostsListDto> createPostsPage(SearchTestHelper<Posts> searchHelper, Pageable pageable) {
        Stream<Posts> filteredPostsStream = searchHelper.getKeywordsFilter();

        return createPageWithContent(filteredPostsStream, pageable);
    }

    private Page<PostsListDto> createPostsPageWithFilter(Predicate<Posts> filter, Pageable pageable) {
        Stream<Posts> postsStream = postsRepository.findAll().stream();
        Stream<Posts> filteredPostsStream = filter == null ? postsStream : postsStream.filter(filter);

        return createPageWithContent(filteredPostsStream, pageable);
    }

    private Page<PostsListDto> createPageWithContent(Stream<Posts> filteredPostsStream, Pageable pageable) {
        return PaginationTestUtil.createPageWithContent(filteredPostsStream, POSTS_CONVERTER, pageable);
    }

    private void assertPostsPage(Page<PostsListDto> result, Page<PostsListDto> postsPage) {
        PaginationTestHelper<PostsListDto, Posts> paginationHelper
                = new PaginationTestHelper<>(result, postsPage, POSTS_CONVERTER);
        paginationHelper.assertContents();
    }

    private <Entity> Entity assertOptionalAndGetEntity(Function<Long, Optional<Entity>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }
}
