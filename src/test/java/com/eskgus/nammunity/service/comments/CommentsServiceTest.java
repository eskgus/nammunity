package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.converter.CommentsConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.PaginationTestHelper;
import com.eskgus.nammunity.helper.Range;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsSaveDto;
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
import java.util.function.Function;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CommentsServiceTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private CommentsService commentsService;

    private User[] users;
    private Posts post;
    private final int page = 1;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDataHelper.signUp(1L, Role.USER);
        User user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDataHelper.signUp(2L, Role.USER);
        User user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        this.users = new User[]{ user1, user2 };

        Long postId = testDataHelper.savePosts(user1);
        this.post = assertOptionalAndGetEntity(postsRepository::findById, postId);
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
        User user = users[0];

        CommentsSaveDto requestDto = new CommentsSaveDto("comment", post.getId());
        Principal principal = user::getUsername;

        Long id = commentsService.save(requestDto, principal);

        Comments comment = assertOptionalAndGetEntity(commentsRepository::findById, id);
        assertSavedComment(requestDto, user, comment);
    }

    private void assertSavedComment(CommentsSaveDto requestDto, User user, Comments comment) {
        assertThat(comment.getContent()).isEqualTo(requestDto.getContent());
        assertThat(comment.getPosts().getId()).isEqualTo(requestDto.getPostsId());
        assertThat(comment.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    public void findByUser() {
        saveComments();

        callAndAssertFindByUser();
    }

    private void saveComments() {
        int numberOfCommentsByUser = 15;
        for (int i = 0; i < numberOfCommentsByUser; i++) {
            for (User user : users) {
                Long commentId = testDataHelper.saveComments(post.getId(), user);
                assertOptionalAndGetEntity(commentsRepository::findById, commentId);
            }
        }
    }

    private void callAndAssertFindByUser() {
        int size = 4;
        User user = users[0];

        Page<CommentsListDto> actualContents = commentsService.findByUser(user, page, size);
        Page<CommentsListDto> expectedContents = createExpectedPageByUser(size, user);

        assertContents(actualContents, expectedContents);
    }

    private Page<CommentsListDto> createExpectedPageByUser(int size, User user) {
        Pageable pageable = createPageable(page, size);
        return commentsRepository.findByUser(user, pageable);
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
        long numberOfComments = 20;
        long half = numberOfComments / 2;

        Range firstRange = Range.builder().startIndex(1).endIndex(half).comment("comment").build();
        Range secondRange = Range.builder().startIndex(half + 1).endIndex(numberOfComments).comment("댓글").build();

        saveCommentsInRange(firstRange);
        saveCommentsInRange(secondRange);
    }

    private void saveCommentsInRange(Range range) {
        for (long i = range.getStartIndex(); i <= range.getEndIndex(); i++) {
            Long commentId = testDataHelper.saveCommentWithContent(post.getId(), users[0], range.getComment() + i);
            assertOptionalAndGetEntity(commentsRepository::findById, commentId);
        }
    }

    private void callAndAssertSearchByContent(String keywords) {
        int size = 3;

        Page<CommentsListDto> actualContents = commentsService.searchByContent(keywords, page, size);
        Page<CommentsListDto> expectedContents = createExpectedContents(keywords, size);

        assertContents(actualContents, expectedContents);
    }

    private Page<CommentsListDto> createExpectedContents(String keywords, int size) {
        Pageable pageable = createPageable(page, size);
        return commentsRepository.searchByContent(keywords, pageable);
    }

    private void assertContents(Page<CommentsListDto> actualContents, Page<CommentsListDto> expectedContents) {
        PaginationTestHelper<CommentsListDto, Comments> paginationHelper
                = new PaginationTestHelper<>(
                        actualContents, expectedContents, new CommentsConverterForTest<>(CommentsListDto.class));
        paginationHelper.assertContents();
    }

    @Test
    public void calculateCommentPage() {
        saveComments();

        callAndAssertCalculateCommentPage();
    }

    private void callAndAssertCalculateCommentPage() {
        // Long postId, Long commentId
        long commentIndex = 3;
        Comments comment = assertOptionalAndGetEntity(
                commentsRepository::findById, commentsRepository.count() - commentIndex);
        int expectedCommentPage = getExpectedCommentPage((int) commentIndex);

        int actualCommentPage = commentsService.calculateCommentPage(post.getId(), comment.getId());

        assertThat(actualCommentPage).isEqualTo(expectedCommentPage);
    }

    private int getExpectedCommentPage(int commentIndex) {
        return commentIndex / 30 + 1;
    }
}
