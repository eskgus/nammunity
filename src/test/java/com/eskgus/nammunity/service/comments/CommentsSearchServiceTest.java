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
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CommentsSearchServiceTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private CommentsSearchService commentsSearchService;

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
    public void findByUser() {
        saveComments();

        callAndAssertFindByUser();
    }

    private void saveComments() {
        int numberOfCommentsByUser = 15;
        for (int i = 0; i < numberOfCommentsByUser; i++) {
            for (User user : users) {
                testDB.saveComments(post.getId(), user);
            }
        }
        assertThat(commentsRepository.count()).isEqualTo((long)numberOfCommentsByUser * users.length);
    }

    private void callAndAssertFindByUser() {
        int size = 4;
        User user = users[0];

        Page<CommentsListDto> actualPage = commentsSearchService.findByUser(user, page, size);
        Page<CommentsListDto> expectedPage = createExpectedPageByUser(size, user);

        assertFindByUser(actualPage, expectedPage);
    }

    private Page<CommentsListDto> createExpectedPageByUser(int size, User user) {
        Pageable pageable = createPageable(page, size);
        return commentsRepository.findByUser(user, pageable);
    }

    private void assertFindByUser(Page<CommentsListDto> actualPage, Page<CommentsListDto> expectedPage) {
        PaginationTestHelper<CommentsListDto, Comments> paginationHelper
                = new PaginationTestHelper<>(actualPage, expectedPage, new CommentsConverterForTest<>(CommentsListDto.class));
        paginationHelper.assertContents();
    }

    @Test
    public void searchByContent() {
        saveCommentsWithContent();

        // 1. 검색 제외 단어 x
        callAndAssertSearchByContent("com 댓");

        // 2. 검색 제외 단어 o
        callAndAssertSearchByContent("com 댓 -ment");
    }

    private void saveCommentsWithContent() {
        long numberOfComments = 20;
        long half = numberOfComments / 2;

        Range firstRange = Range.builder().startIndex(1).endIndex(half).content("comment").build();
        Range secondRange = Range.builder().startIndex(half + 1).endIndex(numberOfComments).content("댓글").build();

        saveCommentsInRange(firstRange);
        saveCommentsInRange(secondRange);
    }

    private void saveCommentsInRange(Range range) {
        for (long i = range.getStartIndex(); i <= range.getEndIndex(); i++) {
            Long commentId = testDB.saveCommentWithContent(post.getId(), users[0], range.getComment() + i);
            assertOptionalAndGetEntity(commentsRepository::findById, commentId);
        }
    }

    private void callAndAssertSearchByContent(String keywords) {
        int size = 3;

        Page<CommentsListDto> actualContents = commentsSearchService.searchByContent(keywords, page, size);
        Page<CommentsListDto> expectedContents = createExpectedContents(keywords, size);

        assertContents(actualContents, expectedContents, CommentsListDto.class);
    }

    private Page<CommentsListDto> createExpectedContents(String keywords, int size) {
        Pageable pageable = createPageable(page, size);
        return commentsRepository.searchByContent(keywords, pageable);
    }

    private <T> void assertContents(Page<T> actualContents, Page<T> expectedContents, Class<T> classOfDto) {
        PaginationTestHelper<T, Comments> paginationHelper
                = new PaginationTestHelper<>(actualContents, expectedContents, new CommentsConverterForTest<>(classOfDto));
        paginationHelper.assertContents();
    }

    @Test
    public void findByPosts() {
        saveComments();

        callAndAssertFindByPosts();
    }

    private void callAndAssertFindByPosts() {
        User user = users[0];

        Page<CommentsReadDto> actualContents = commentsSearchService.findByPosts(post, user, page);
        Page<CommentsReadDto> expectedContents = createExpectedPageByPosts();

        assertFindByPosts(actualContents, expectedContents, user);
    }

    private Page<CommentsReadDto> createExpectedPageByPosts() {
        Pageable pageable = createPageable(page, 30);
        return commentsRepository.findByPosts(post, pageable);
    }

    private void assertFindByPosts(Page<CommentsReadDto> actualContents, Page<CommentsReadDto> expectedContents, User user) {
        assertContents(actualContents, expectedContents, CommentsReadDto.class);
        assertBooleanValues(actualContents.getContent(), user);
    }

    private void assertBooleanValues(List<CommentsReadDto> comments, User user) {
        List<Boolean> expectedDoesUserWriteComment = getExpectedDoesUserWriteComment(user);
        List<Boolean> expectedDoesUserLikeComment = getExpectedDoesUserLikeComment();

        for (int i = 0; i < comments.size(); i++) {
            CommentsReadDto comment = comments.get(i);
            assertThat(comment.isDoesUserWriteComment()).isEqualTo(expectedDoesUserWriteComment.get(i));
            assertThat(comment.isDoesUserLikeComment()).isEqualTo(expectedDoesUserLikeComment.get(i));
        }
    }

    private List<Boolean> getExpectedDoesUserWriteComment(User user) {
        List<Boolean> expectedDoesUserWriteComment = new ArrayList<>();
        for (long i = commentsRepository.count(); i >= 1; i--) {
            Comments comment = assertOptionalAndGetEntity(commentsRepository::findById, i);
            Long authorId = comment.getUser().getId();
            Long userId = user.getId();
            expectedDoesUserWriteComment.add(authorId.equals(userId));
        }
        return expectedDoesUserWriteComment;
    }

    private List<Boolean> getExpectedDoesUserLikeComment() {
        List<Boolean> expectedDoesUserLikeComment = new ArrayList<>();
        for (long i = 0; i < commentsRepository.count(); i++) {
            expectedDoesUserLikeComment.add(false); // 댓글 좋아요 안 함
        }
        return expectedDoesUserLikeComment;
    }
}
