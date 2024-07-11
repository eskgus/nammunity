package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.config.TestConfig;
import com.eskgus.nammunity.converter.LikesConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.helper.PaginationTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.util.PaginationRepoUtil;
import com.eskgus.nammunity.util.PaginationTestUtil;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
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
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({ TestDataHelper.class, TestConfig.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class LikesRepositoryTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private LikesRepository likesRepository;

    private User[] users;
    private Posts post1;
    private Comments comment1;
    private static final int PAGE = 1;
    private static final LikesConverterForTest LIKES_CONVERTER = new LikesConverterForTest();
    private static final LikesTestVisitor VISITOR = new LikesTestVisitor(LIKES_CONVERTER);

    @BeforeEach
    public void setUp() {
        Long user1Id = testDataHelper.signUp(1L, Role.USER);
        User user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDataHelper.signUp(2L, Role.USER);
        User user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        this.users = new User[]{ user1, user2 };

        Long post1Id = testDataHelper.savePosts(user1);
        this.post1 = assertOptionalAndGetEntity(postsRepository::findById, post1Id);

        Long comment1Id = testDataHelper.saveComments(post1Id, user1);
        this.comment1 = assertOptionalAndGetEntity(commentsRepository::findById, comment1Id);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void existsLikesByPostsAndUserWithoutLikes() {
        testExistsLikesByPostsAndUser(false);
    }

    @Test
    public void existsLikesByPostsAndUserWithLikes() {
        savePostLikes(users[0]);

        testExistsLikesByPostsAndUser(true);
    }

    @Test
    public void existsLikesByCommentsAndUserWithoutLikes() {
        testExistsLikesByCommentsAndUser(false);
    }

    @Test
    public void existsLikesByCommentsAndUserWithLikes() {
        saveCommentLikes(users[0]);

        testExistsLikesByCommentsAndUser(true);
    }

    @Test
    public void findLikesByUser() {
        testFindContentLikesByUser(null, likesRepository::findByUser);
    }

    @Test
    public void findPostLikesByUser() {
        testFindContentLikesByUser(post1, likesRepository::findPostLikesByUser);
    }

    @Test
    public void findCommentLikesByUser() {
        testFindContentLikesByUser(comment1, likesRepository::findCommentLikesByUser);
    }

    @Test
    public void deleteLikesByPosts() {
        testDeleteLikesByContent(this::savePostLikes, likesRepository::deleteByPosts, post1);
    }

    @Test
    public void deleteLikesByComments() {
        testDeleteLikesByContent(this::saveCommentLikes, likesRepository::deleteByComments, comment1);
    }

    private void testExistsLikesByPostsAndUser(boolean exists) {
        testExistsLikesByContentAndUser(likesRepository::existsByPostsAndUser, post1, exists);
    }

    private void testExistsLikesByCommentsAndUser(boolean exists) {
        testExistsLikesByContentAndUser(likesRepository::existsByCommentsAndUser, comment1, exists);
    }

    private <Content> void testExistsLikesByContentAndUser(BiFunction<Content, User, Boolean> checker, Content content,
                                                           boolean exists) {
        // given
        User user = users[0];

        // when
        boolean result = checker.apply(content, user);

        // then
        assertEquals(exists, result);
    }

    private void testFindContentLikesByUser(Element element, BiFunction<User, Pageable, Page<LikesListDto>> finder) {
        // given
        saveLikes();

        User user = users[0];

        Pageable pageable = createPageable();

        Predicate<Likes> filter = element != null ? createFilter(element, user) : createFilter(user);
        Page<LikesListDto> likesPage = createLikesPage(filter, pageable);

        // when
        Page<LikesListDto> result = finder.apply(user, pageable);

        // then
        assertLikesPage(result, likesPage);
    }

    private <Content> void testDeleteLikesByContent(Function<User, Long> saver,
                                                    BiConsumer<Content, User> deleter, Content content) {
        // given
        User user = users[0];

        Long likeId = saver.apply(user);

        // when
        deleter.accept(content, user);

        // then
        boolean result = likesRepository.existsById(likeId);
        assertFalse(result);
    }

    private void saveLikes() {
        for (User user : users) {
            savePostLikes(user);
            saveCommentLikes(user);
        }
    }

    private Long savePostLikes(User user) {
        Long postLikeId = testDataHelper.savePostLikes(post1.getId(), user);
        assertOptionalAndGetEntity(likesRepository::findById, postLikeId);

        return postLikeId;
    }

    private Long saveCommentLikes(User user) {
        Long commentLikeId = testDataHelper.saveCommentLikes(comment1.getId(), user);
        assertOptionalAndGetEntity(likesRepository::findById, commentLikeId);

        return commentLikeId;
    }

    private Pageable createPageable() {
        return PaginationRepoUtil.createPageable(PAGE, 3);
    }

    private Predicate<Likes> createFilter(Element element, User user) {
        element.accept(VISITOR);

        Predicate<Likes> filter = VISITOR.getFilter();
        return filter.and(createFilter(user));
    }

    private Predicate<Likes> createFilter(User user) {
        user.accept(VISITOR);

        return VISITOR.getFilter();
    }

    private Page<LikesListDto> createLikesPage(Predicate<Likes> filter, Pageable pageable) {
        Stream<Likes> filteredLikesStream = likesRepository.findAll().stream().filter(filter);

        return createPageWithContent(filteredLikesStream, pageable);
    }

    private Page<LikesListDto> createPageWithContent(Stream<Likes> filteredLikesStream, Pageable pageable) {
        return PaginationTestUtil.createPageWithContent(filteredLikesStream, LIKES_CONVERTER, pageable);
    }

    private void assertLikesPage(Page<LikesListDto> result, Page<LikesListDto> likesPage) {
        PaginationTestHelper<LikesListDto, Likes> paginationHelper
                = new PaginationTestHelper<>(result, likesPage, LIKES_CONVERTER);
        paginationHelper.assertContents();
    }

    private <Entity> Entity assertOptionalAndGetEntity(Function<Long, Optional<Entity>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }
}
