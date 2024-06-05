package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.config.TestSecurityConfig;
import com.eskgus.nammunity.converter.LikesConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.helper.PaginationTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
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

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;
import static com.eskgus.nammunity.util.PaginationTestUtil.createPageWithContent;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({ TestDataHelper.class, TestSecurityConfig.class })
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
    private final int page = 1;
    private final LikesConverterForTest entityConverter = new LikesConverterForTest();

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

        saveLikes();
    }

    private void saveLikes() {
        for (User user : users) {
            Long postLikeId = testDataHelper.savePostLikes(post1.getId(), user);
            assertOptionalAndGetEntity(likesRepository::findById, postLikeId);

            Long commentLikeId = testDataHelper.saveCommentLikes(comment1.getId(), user);
            assertOptionalAndGetEntity(likesRepository::findById, commentLikeId);
        }
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void findByUser() {
        callAndAssertFindLikesByUser();
    }

    private void callAndAssertFindLikesByUser() {
        int size = 3;
        User user = users[0];

        Pageable pageable = createPageable(page, size);

        Page<LikesListDto> actualContents = likesRepository.findByUser(user, pageable);
        Page<LikesListDto> expectedContents = createExpectedContentsByUser(user, pageable);

        assertContents(actualContents, expectedContents);
    }

    private Page<LikesListDto> createExpectedContentsByUser(User user, Pageable pageable) {
        Predicate<Likes> filter = createFilterByUser(user);
        return createExpectedContents(filter, pageable);
    }

    private Predicate<Likes> createFilterByUser(User user) {
        return likes -> entityConverter.extractUserId(likes).equals(user.getId());
    }

    private Page<LikesListDto> createExpectedContents(Predicate<Likes> filter, Pageable pageable) {
        Stream<Likes> filteredLikesStream = likesRepository.findAll().stream().filter(filter);
        return createPageWithContent(filteredLikesStream, entityConverter, pageable);
    }

    private void assertContents(Page<LikesListDto> actualContents, Page<LikesListDto> expectedContents) {
        PaginationTestHelper<LikesListDto, Likes> paginationHelper
                = new PaginationTestHelper<>(actualContents, expectedContents, entityConverter);
        paginationHelper.assertContents();
    }

    @Test
    public void findPostLikesByUser() {
        callAndAssertFindLikesByUser(likesRepository::findPostLikesByUser, ContentType.POSTS);
    }

    private void callAndAssertFindLikesByUser(BiFunction<User, Pageable, Page<LikesListDto>> finder,
                                              ContentType contentType) {
        int size = 3;
        User user = users[0];

        Pageable pageable = createPageable(page, size);

        Page<LikesListDto> actualContents = finder.apply(user, pageable);
        Page<LikesListDto> expectedContents = createExpectedContentsByUser(user, pageable, contentType);

        assertContents(actualContents, expectedContents);
    }

    private Page<LikesListDto> createExpectedContentsByUser(User user, Pageable pageable, ContentType contentType) {
        Predicate<Likes> filter;
        if (contentType.equals(ContentType.POSTS)) {
            filter = likes -> entityConverter.getPosts(likes) != null;
        } else {
            filter = likes -> entityConverter.getComments(likes) != null;
        }

        Predicate<Likes> userFilter = createFilterByUser(user);
        filter = filter.and(userFilter);
        return createExpectedContents(filter, pageable);
    }

    @Test
    public void findCommentLikesByUser() {
        callAndAssertFindLikesByUser(likesRepository::findCommentLikesByUser, ContentType.COMMENTS);
    }

    @Test
    public void deleteByPosts() {
        Posts post2 = savePost();

        callAndAssertDeleteByContent(testDataHelper::savePostLikes, likesRepository::deleteByPosts, post2);
    }

    private Posts savePost() {
        Long postId = testDataHelper.savePosts(users[0]);
        return assertOptionalAndGetEntity(postsRepository::findById, postId);
    }

    private <T> void callAndAssertDeleteByContent(BiFunction<Long, User, Long> likesSaver,
                                                  BiConsumer<T, User> likesDeleter,
                                                  T content) {
        Long contentId = getContentId(content);

        Long likeId = likesSaver.apply(contentId, users[0]);
        assertOptionalAndGetEntity(likesRepository::findById, likeId);

        likesDeleter.accept(content, users[0]);
        assertDeleteByContent(likeId);
    }

    private <T> Long getContentId(T content) {
        if (content instanceof Posts) {
            return ((Posts) content).getId();
        } else {
            return  ((Comments) content).getId();
        }
    }

    private void assertDeleteByContent(Long likeId) {
        boolean result = likesRepository.existsById(likeId);
        assertThat(result).isFalse();
    }

    @Test
    public void deleteByComments() {
        Comments comment2 = saveComment();

        callAndAssertDeleteByContent(testDataHelper::saveCommentLikes, likesRepository::deleteByComments, comment2);
    }

    private Comments saveComment() {
        Long commentId = testDataHelper.saveComments(post1.getId(), users[0]);
        return assertOptionalAndGetEntity(commentsRepository::findById, commentId);
    }

    @Test
    public void existsByPostsAndUser() {
        // 1. user1이 post1 좋아요 후 호출
        callAndAssertExistsByContentsAndUser(post1, users[0], true);

        // 2. user2가 post2 좋아요 x 후 호출
        Posts post2 = savePost();
        callAndAssertExistsByContentsAndUser(post2, users[1], false);
    }

    private <T> void callAndAssertExistsByContentsAndUser(T content, User user, boolean expectedDoesUserLikeContent) {
        boolean actualDoesUserLikeContent = callExistsByContentsAndUser(content, user);
        assertThat(actualDoesUserLikeContent).isEqualTo(expectedDoesUserLikeContent);
    }

    private <T> boolean callExistsByContentsAndUser(T content, User user) {
        if (content instanceof Posts) {
            return likesRepository.existsByPostsAndUser((Posts) content, user);
        }
        return likesRepository.existsByCommentsAndUser((Comments) content, user);
    }

    @Test
    public void existsByCommentsAndUser() {
        // 1. user1이 comment1 좋아요 후 호출
        callAndAssertExistsByContentsAndUser(comment1, users[0], true);

        // 2. user2가 comment2 좋아요 x 후 호출
        Comments comment2 = saveComment();
        callAndAssertExistsByContentsAndUser(comment2, users[1], false);
    }
}
