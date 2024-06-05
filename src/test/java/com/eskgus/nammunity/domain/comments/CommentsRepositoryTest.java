package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.config.TestSecurityConfig;
import com.eskgus.nammunity.converter.CommentsConverterForTest;
import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.helper.*;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
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
    private final int page = 1;

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

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void countByUser() {
        // 1. 댓글 작성 x 후 호출
        callAndAssertCountByUser(0L);

        // 2. 댓글 1개 작성 후 호출
        Comments comment = saveCommentAndGetSavedComment();
        callAndAssertCountByUser(comment.getId());
    }

    private void callAndAssertCountByUser(Long expectedCount) {
        Long actualCount = commentsRepository.countByUser(users[0]);
        assertThat(actualCount).isEqualTo(expectedCount);
    }

    private Comments saveCommentAndGetSavedComment() {
        Long commentId = testDataHelper.saveComments(posts[0].getId(), users[0]);
        return assertOptionalAndGetEntity(commentsRepository::findById, commentId);
    }

    @Test
    public void searchByContent() {
        saveCommentsWithContent();

        // 1. 검색 제외 단어 x
        callAndAssertSearchByContent("com 댓");

        // 2. 검색 제외 단어 o
        callAndAssertSearchByContent("com 댓 -ent");
    }

    private void saveCommentsWithContent() {
        long numberOfCommentsByUserAndPosts = 10;
        long half = numberOfCommentsByUserAndPosts / 2;

        Range firstRange = Range.builder().startIndex(1).endIndex(half).comment("comment").build();
        Range secondRage = Range.builder().startIndex(half + 1).endIndex(numberOfCommentsByUserAndPosts).comment("댓글").build();

        saveCommentsInRange(firstRange);
        saveCommentsInRange(secondRage);
    }

    private void saveCommentsInRange(Range range) {
        for (int i = 0; i < posts.length; i++) {
            for (long j = range.getStartIndex(); j <= range.getEndIndex(); j++) {
                Long commentId
                        = testDataHelper.saveCommentWithContent(posts[i].getId(), users[i], range.getComment() + j);
                assertOptionalAndGetEntity(commentsRepository::findById, commentId);
            }
        }
    }

    private void callAndAssertSearchByContent(String keywords) {
        int size = 3;

        Pageable pageable = createPageable(page, size);
        CommentsConverterForTest<CommentsListDto> entityConverter = new CommentsConverterForTest<>(CommentsListDto.class);

        Page<CommentsListDto> actualContents = commentsRepository.searchByContent(keywords, pageable);
        Page<CommentsListDto> expectedContents
                = createExpectedContentsByContent(entityConverter, keywords, pageable, Comments::getContent);

        assertContents(actualContents, expectedContents, entityConverter);
    }

    private Page<CommentsListDto> createExpectedContentsByContent(CommentsConverterForTest<CommentsListDto> entityConverter,
                                                                  String keywords, Pageable pageable,
                                                                  Function<Comments, String>... fieldExtractor) {
        SearchTestHelper<Comments> searchHelper = SearchTestHelper.<Comments>builder()
                .totalContents(commentsRepository.findAll()).keywords(keywords)
                .fieldExtractors(fieldExtractor).build();
        Stream<Comments> filteredCommentsStream = searchHelper.getKeywordsFilter();

        return createPageWithContent(filteredCommentsStream, entityConverter, pageable);
    }

    private <T> void assertContents(Page<T> actualContents, Page<T> expectedContents,
                                    EntityConverterForTest<T, Comments>  entityConverter) {
        PaginationTestHelper<T, Comments> paginationHelper
                = new PaginationTestHelper<>(actualContents, expectedContents, entityConverter);
        paginationHelper.assertContents();
    }

    @Test
    public void findByUser() {
        saveCommentsWithContent();

        callAndAssertFindByUser();
    }

    private void callAndAssertFindByUser() {
        int size = 4;
        User user = users[0];

        Pageable pageable = createPageable(page, size);
        CommentsConverterForTest<CommentsListDto> entityConverter = new CommentsConverterForTest<>(CommentsListDto.class);

        Page<CommentsListDto> actualContents = commentsRepository.findByUser(user, pageable);
        Page<CommentsListDto> expectedContents = createExpectedContentsByUser(entityConverter, user, pageable);

        assertContents(actualContents, expectedContents, entityConverter);
    }

    private Page<CommentsListDto> createExpectedContentsByUser(CommentsConverterForTest<CommentsListDto> entityConverter,
                                                               User user, Pageable pageable) {
        Predicate<Comments> filter = comment -> entityConverter.extractUserId(comment).equals(user.getId());
        return createExpectedContents(filter, entityConverter, pageable);
    }

    private <T> Page<T> createExpectedContents(Predicate<Comments> filter,
                                               CommentsConverterForTest<T> entityConverter,
                                               Pageable pageable) {
        Stream<Comments> filteredCommentsStream = commentsRepository.findAll().stream().filter(filter);
        return createPageWithContent(filteredCommentsStream, entityConverter, pageable);
    }

    @Test
    public void findByPosts() {
        saveCommentsWithContent();

        callAndAssertFindByPosts();
    }

    private void callAndAssertFindByPosts() {
        int size = 4;
        Posts post = posts[0];

        Pageable pageable = createPageable(page, size);
        CommentsConverterForTest<CommentsReadDto> entityConverter = new CommentsConverterForTest<>(CommentsReadDto.class);

        Page<CommentsReadDto> actualContents = commentsRepository.findByPosts(post, pageable);
        Page<CommentsReadDto> expectedContents = createExpectedContentsByPosts(entityConverter, post, pageable);

        assertContents(actualContents, expectedContents, entityConverter);
    }

    private Page<CommentsReadDto> createExpectedContentsByPosts(CommentsConverterForTest<CommentsReadDto> entityConverter,
                                                                Posts post, Pageable pageable) {
        Predicate<Comments> filter = comment -> entityConverter.extractPostId(comment).equals(post.getId());
        return createExpectedContents(filter, entityConverter, pageable);
    }

    @Test
    public void countCommentIndex() {
        saveCommentsWithContent();

        callAndAssertCountCommentIndex();
    }

    private void callAndAssertCountCommentIndex() {
        long expectedCommentIndex = 3;
        Comments comment = assertOptionalAndGetEntity(
                commentsRepository::findById, commentsRepository.count() - expectedCommentIndex);
        // 댓글 마지막 - 5개가 post2의 댓글이라 post2를 postId로 하는 거
        long actualCommentIndex = commentsRepository.countCommentIndex(posts[1].getId(), comment.getId());
        assertThat(actualCommentIndex).isEqualTo(expectedCommentIndex);
    }
}
