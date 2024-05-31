package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.converter.CommentsConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.ContentsPageDtoTestHelper;
import com.eskgus.nammunity.helper.PaginationTestHelper;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CommentsViewServiceTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private CommentsViewService commentsViewService;

    @Autowired
    private CommentsService commentsService;

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
    public void findCommentsPageByPosts() {
        saveComments();

        callAndAssertFindByPosts();
    }

    private void callAndAssertFindByPosts() {
        User user = users[0];

        Page<CommentsReadDto> actualContents = commentsViewService.findCommentsPageByPosts(post, user, page);
        Page<CommentsReadDto> expectedContents = createExpectedPageByPosts();

        assertFindByPosts(actualContents, expectedContents, user);
    }

    private Page<CommentsReadDto> createExpectedPageByPosts() {
        Pageable pageable = createPageable(page, 30);
        return commentsRepository.findByPosts(post, pageable);
    }

    private void assertFindByPosts(Page<CommentsReadDto> actualContents, Page<CommentsReadDto> expectedContents, User user) {
        assertContents(actualContents, expectedContents);
        assertBooleanValues(actualContents.getContent(), user);
    }

    private void assertContents(Page<CommentsReadDto> actualContents, Page<CommentsReadDto> expectedContents) {
        PaginationTestHelper<CommentsReadDto, Comments> paginationHelper
                = new PaginationTestHelper<>(actualContents, expectedContents, new CommentsConverterForTest<>(CommentsReadDto.class));
        paginationHelper.assertContents();
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

    @Test
    public void listComments() {
        saveComments();

        callAndAssertListComments();
    }

    private void saveComments() {
        int numberOfCommentsByUser = 20;
        for (int i = 0; i < numberOfCommentsByUser; i++) {
            for (User user : users) {
                Long commentId = testDB.saveComments(post.getId(), user);
                assertOptionalAndGetEntity(commentsRepository::findById, commentId);
            }
        }
    }

    private void callAndAssertListComments() {
        User user = users[0];
        int page = 1;
        ContentsPageDto<CommentsListDto> actualResult = callListCommentsAndGetActualResult(user, page);
        Page<CommentsListDto> expectedContents = commentsService.findByUser(user, page, 20);

        ContentsPageDtoTestHelper<CommentsListDto, Comments> findHelper = ContentsPageDtoTestHelper.<CommentsListDto, Comments>builder()
                .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(new CommentsConverterForTest<>(CommentsListDto.class)).build();
        findHelper.createExpectedResultAndAssertContentsPage();
    }

    private ContentsPageDto<CommentsListDto> callListCommentsAndGetActualResult(User user, int page) {
        Principal principal = user::getUsername;
        return commentsViewService.listComments(principal, page);
    }
}
