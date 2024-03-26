package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.converter.CommentsConverterForTest;
import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.FindHelperForTest;
import com.eskgus.nammunity.helper.repository.ServiceTriFinderForTest;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.eskgus.nammunity.util.FindUtilForTest.callAndAssertFind;
import static com.eskgus.nammunity.util.FindUtilForTest.initializeFindHelper;
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
    public void findByUser() {
        saveComments();

        callAndAssertFindComments();
    }

    private void saveComments() {
        int numberOfCommentsByUser = 15;
        for (int i = 0; i < numberOfCommentsByUser; i++) {
            for (User user : users) {
                testDB.saveComments(post.getId(), user);
            }
        }
        assertThat(commentsRepository.count()).isEqualTo(numberOfCommentsByUser * users.length);
    }

    private void callAndAssertFindComments() {
        FindHelperForTest<ServiceTriFinderForTest<CommentsListDto>, Comments, CommentsListDto, User> findHelper
                = createTriFindHelper();

        initializeFindHelper(findHelper);
        callAndAssertFind();
    }

    private FindHelperForTest<ServiceTriFinderForTest<CommentsListDto>, Comments, CommentsListDto, User>
        createTriFindHelper() {
        EntityConverterForTest<Comments, CommentsListDto> entityConverter = new CommentsConverterForTest();
        return FindHelperForTest.<ServiceTriFinderForTest<CommentsListDto>, Comments, CommentsListDto, User>builder()
                .finder(commentsSearchService::findByUser)
                .contents(users[0])
                .entityStream(commentsRepository.findAll().stream())
                .page(1).limit(4)
                .entityConverter(entityConverter).build();
    }
}
