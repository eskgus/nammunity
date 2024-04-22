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
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CommentsServiceTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private CommentsService commentsService;

    @Autowired
    private CommentsSearchService commentsSearchService;

    private User[] users;
    private Posts post;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        Long user2Id = testDB.signUp(2L, Role.USER);
        assertThat(userRepository.count()).isEqualTo(user2Id);

        User user1 = userRepository.findById(user1Id).get();
        User user2 = userRepository.findById(user2Id).get();

        this.users = new User[]{ user1, user2 };

        Long postId = testDB.savePosts(user1);
        assertThat(postsRepository.count()).isEqualTo(postId);

        this.post = postsRepository.findById(postId).get();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void listPosts() {
        saveComments();

        callAndAssertListComments();
    }

    private void saveComments() {
        int numberOfCommentsByUser = 20;
        for (int i = 0; i < numberOfCommentsByUser; i++) {
            for (User user : users) {
                testDB.saveComments(post.getId(), user);
            }
        }
        assertThat(commentsRepository.count()).isEqualTo((long) numberOfCommentsByUser * users.length);
    }

    private void callAndAssertListComments() {
        User user = users[0];
        int page = 1;
        ContentsPageDto<CommentsListDto> actualResult = callListCommentsAndGetActualResult(user, page);
        Page<CommentsListDto> expectedContents = commentsSearchService.findByUser(user, page, 20);

        ContentsPageDtoTestHelper<CommentsListDto, Comments> findHelper = ContentsPageDtoTestHelper.<CommentsListDto, Comments>builder()
                .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(new CommentsConverterForTest<>(CommentsListDto.class)).build();
        findHelper.createExpectedResultAndAssertContentsPage();
    }

    private ContentsPageDto<CommentsListDto> callListCommentsAndGetActualResult(User user, int page) {
        Principal principal = user::getUsername;
        return commentsService.listComments(principal, page);
    }
}
