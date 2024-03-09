package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.converter.PostsConverterForTest;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.FindHelperForTest;
import com.eskgus.nammunity.helper.repository.ServiceFinderForTest;
import com.eskgus.nammunity.helper.repository.ServiceTriFinderForTest;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
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
public class PostsSearchServiceTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private PostsSearchService postsSearchService;

    private User[] users;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        Long user2Id = testDB.signUp(2L, Role.USER);
        assertThat(userRepository.count()).isEqualTo(user2Id);

        User user1 = userRepository.findById(user1Id).get();
        User user2 = userRepository.findById(user2Id).get();

        this.users = new User[]{ user1, user2 };
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void findAllDesc() {
        savePosts();

        FindHelperForTest<ServiceFinderForTest<PostsListDto>, Posts, PostsListDto> findHelper = createFindHelper();
        callAndAssertFindPosts(findHelper);
    }

    private void savePosts() {
        int numberOfPostsByUser = 15;
        for (int i = 0; i < numberOfPostsByUser; i++) {
            for (User user : users) {
                testDB.savePosts(user);
            }
        }
        assertThat(postsRepository.count()).isEqualTo(numberOfPostsByUser * users.length);
    }

    private FindHelperForTest<ServiceFinderForTest<PostsListDto>, Posts, PostsListDto> createFindHelper() {
        EntityConverterForTest<Posts, PostsListDto> entityConverter = new PostsConverterForTest();
        return FindHelperForTest.<ServiceFinderForTest<PostsListDto>, Posts, PostsListDto>builder()
                .finder(postsSearchService::findAllDesc)
                .entityStream(postsRepository.findAll().stream())
                .page(1).limit(20)
                .entityConverter(entityConverter).build();
    }

    private void callAndAssertFindPosts(FindHelperForTest findHelper) {
        initializeFindHelper(findHelper);
        callAndAssertFind();
    }

    @Test
    public void findByUser() {
        savePosts();

        FindHelperForTest<ServiceTriFinderForTest<PostsListDto>, Posts, PostsListDto> findHelper = createTriFindHelper();
        callAndAssertFindPosts(findHelper);
    }

    private FindHelperForTest<ServiceTriFinderForTest<PostsListDto>, Posts, PostsListDto> createTriFindHelper() {
        EntityConverterForTest<Posts, PostsListDto> entityConverter = new PostsConverterForTest();
        return FindHelperForTest.<ServiceTriFinderForTest<PostsListDto>, Posts, PostsListDto>builder()
                .finder(postsSearchService::findByUser).user(users[0])
                .entityStream(postsRepository.findAll().stream())
                .page(1).limit(4)
                .entityConverter(entityConverter).build();
    }
}
