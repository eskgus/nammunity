package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.config.TestConfig;
import com.eskgus.nammunity.converter.CommentsConverterForTest;
import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.helper.*;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.util.PaginationRepoUtil;
import com.eskgus.nammunity.util.PaginationTestUtil;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
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

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({ TestDataHelper.class, TestConfig.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CommentsRepositoryTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    private User[] users;
    private Posts[] posts;
    private static final int PAGE = 1;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDataHelper.signUp(1L, Role.USER);
        User user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDataHelper.signUp(2L, Role.USER);
        User user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        this.users = new User[]{ user1, user2 };

        Long post1Id = testDataHelper.savePosts(user1);
        Posts post1 = assertOptionalAndGetEntity(postsRepository::findById, post1Id);

        Long post2Id = testDataHelper.savePosts(user1);
        Posts post2 = assertOptionalAndGetEntity(postsRepository::findById, post2Id);

        this.posts = new Posts[]{ post1, post2 };
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void countCommentsByUserWithoutComments() {
        // given
        // when/then
        testCountCommentsByUser(users[0], 0L);
    }

    @Test
    public void countCommentsByUserWithComments() {
        // given
        long numberOfComments = saveComments();

        // when/then
        testCountCommentsByUser(users[0], numberOfComments);
    }

    @Test
    public void searchCommentsByContentWithoutExcludeKeywords() {
        testSearchCommentsByContent("com 댓");
    }

    @Test
    public void searchCommentsByContentWithExcludeKeywords() {
        testSearchCommentsByContent("com 댓 -ent");
    }

    @Test
    public void findCommentsByUser() {
        testFindCommentsByElement(users[0], CommentsListDto.class, commentsRepository::findByUser);
    }

    @Test
    public void findCommentsByPosts() {
        testFindCommentsByElement(posts[0], CommentsReadDto.class, commentsRepository::findByPosts);
    }

    @Test
    public void countCommentIndex() {
        // given
        long numberOfCommentsByUserAndPosts = saveCommentsWithContent();

        Posts post = posts[0];

        Comments comment = assertOptionalAndGetEntity(commentsRepository::findById, 1L);

        long commentIndex = numberOfCommentsByUserAndPosts - 1;

        // when
        long result = commentsRepository.countCommentIndex(post.getId(), comment.getId());

        // then
        assertEquals(commentIndex, result);
    }

    private void testCountCommentsByUser(User user, long numberOfComments) {
        // when
        long result = commentsRepository.countByUser(user);

        // then
        assertEquals(numberOfComments, result);
    }

    private void testSearchCommentsByContent(String keywords) {
        // given
        saveCommentsWithContent();

        Pageable pageable = createPageable();

        CommentsConverterForTest<CommentsListDto> commentsConverter = createCommentsConverter(CommentsListDto.class);

        SearchTestHelper<Comments> searchHelper = createSearchHelper(keywords, Comments::getContent);
        Page<CommentsListDto> commentsPage = createCommentsPage(searchHelper, commentsConverter, pageable);

        // when
        Page<CommentsListDto> result = commentsRepository.searchByContent(keywords, pageable);

        // then
        assertCommentsPage(result, commentsPage, commentsConverter);
    }

    private <ElementType extends Element, Dto> void testFindCommentsByElement(ElementType element, Class<Dto> dtoType,
                                                                              BiFunction<ElementType, Pageable, Page<Dto>> finder) {
        // given
        saveCommentsWithContent();

        Pageable pageable = createPageable();

        CommentsConverterForTest<Dto> commentsConverter = createCommentsConverter(dtoType);

        Predicate<Comments> filter = createFilter(commentsConverter, element);
        Page<Dto> commentsPage = createCommentsPage(filter, commentsConverter, pageable);

        // when
        Page<Dto> result = finder.apply(element, pageable);

        // then
        assertCommentsPage(result, commentsPage, commentsConverter);
    }

    private long saveComments() {
        long numberOfComments = 3L;

        for (long i = 0; i < numberOfComments; i++) {
            Long commentId = testDataHelper.saveComments(posts[0].getId(), users[0]);
            assertOptionalAndGetEntity(commentsRepository::findById, commentId);
        }

        return numberOfComments;
    }

    private Pageable createPageable() {
        return PaginationRepoUtil.createPageable(PAGE, 3);
    }

    private long saveCommentsWithContent() {
        long numberOfCommentsByUserAndPosts = 10;
        long half = numberOfCommentsByUserAndPosts / 2;

        Range firstRange = Range.builder().startIndex(1).endIndex(half).comment("comment").build();
        Range secondRage = Range.builder()
                .startIndex(half + 1).endIndex(numberOfCommentsByUserAndPosts).comment("댓글").build();

        saveCommentsInRange(firstRange);
        saveCommentsInRange(secondRage);

        return numberOfCommentsByUserAndPosts;
    }

    private void saveCommentsInRange(Range range) {
        for (int i = 0; i < posts.length; i++) {
            for (long j = range.getStartIndex(); j <= range.getEndIndex(); j++) {
                Long commentId = testDataHelper.saveCommentWithContent(
                        posts[i].getId(), users[i], range.getComment() + j);
                assertOptionalAndGetEntity(commentsRepository::findById, commentId);
            }
        }
    }

    private SearchTestHelper<Comments> createSearchHelper(String keywords,
                                                          Function<Comments, String>... fieldExtractors) {
        return SearchTestHelper.<Comments>builder()
                .totalContents(commentsRepository.findAll()).keywords(keywords).fieldExtractors(fieldExtractors).build();
    }

    private <Dto> CommentsConverterForTest<Dto> createCommentsConverter(Class<Dto> dtoType) {
        return new CommentsConverterForTest<>(dtoType);
    }

    private <Dto> Predicate<Comments> createFilter(CommentsConverterForTest<Dto> commentsConverter, Element element) {
        CommentsTestVisitor<Dto> visitor = new CommentsTestVisitor<>(commentsConverter);
        element.accept(visitor);

        return visitor.getFilter();
    }

    private Page<CommentsListDto> createCommentsPage(SearchTestHelper<Comments> searchHelper,
                                                     CommentsConverterForTest<CommentsListDto> commentsConverter,
                                                     Pageable pageable) {
        Stream<Comments> filteredCommentsStream = searchHelper.getKeywordsFilter();

        return createPageWithContent(filteredCommentsStream, commentsConverter, pageable);
    }

    private <Dto> Page<Dto> createCommentsPage(Predicate<Comments> filter,
                                               CommentsConverterForTest<Dto> commentsConverter, Pageable pageable) {
        Stream<Comments> filteredCommentsStream = commentsRepository.findAll().stream().filter(filter);

        return createPageWithContent(filteredCommentsStream, commentsConverter, pageable);
    }

    private <Dto> Page<Dto> createPageWithContent(Stream<Comments> filteredCommentsStream,
                                                  CommentsConverterForTest<Dto> commentsConverter,
                                                  Pageable pageable) {
        return PaginationTestUtil.createPageWithContent(filteredCommentsStream, commentsConverter, pageable);
    }

    private <Dto> void assertCommentsPage(Page<Dto> result, Page<Dto> commentsPage,
                                          EntityConverterForTest<Dto, Comments> commentsConverter) {
        PaginationTestHelper<Dto, Comments> paginationHelper
                = new PaginationTestHelper<>(result, commentsPage, commentsConverter);
        paginationHelper.assertContents();
    }

    private <Entity> Entity assertOptionalAndGetEntity(Function<Long, Optional<Entity>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }
}
