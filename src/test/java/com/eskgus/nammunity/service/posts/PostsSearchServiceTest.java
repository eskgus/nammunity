package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.eskgus.nammunity.util.FinderUtil.assertPageForServiceTest;

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

    @BeforeEach
    public void setUp() {
        // 1. user1 회원가입
        testDB.signUp(1L, Role.USER);
        Assertions.assertThat(userRepository.count()).isOne();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void findAllDesc() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성 * 21
        long expectedTotalElements = 0;
        for (int i = 0; i < 21; i++) {
            expectedTotalElements = testDB.savePosts(user1);
        }
        Assertions.assertThat(postsRepository.count()).isEqualTo(expectedTotalElements);

        // 3. page = 2로 해서 findAllDesc() 호출
        Page<PostsListDto> posts = postsSearchService.findAllDesc(2);

        // 4. expectedTotalElements = 전체 게시글 수로 해서 결과 검증
        assertPageForServiceTest(posts, expectedTotalElements);
    }

    @Test
    public void findByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(testDB.signUp(2L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isEqualTo(2);

        // 2. user1이 게시글 작성 * 3
        long expectedTotalElements = 0;
        for (int i = 0; i < 3; i++) {
            expectedTotalElements = testDB.savePosts(user1);
        }

        // 3. user2가 게시글 작성 * 1
        Long latestPostId = testDB.savePosts(user2);
        Assertions.assertThat(postsRepository.count()).isEqualTo(latestPostId);

        // 4. user = user1, page = 2, size = 2로 해서 findByUser() 호출
        Page<PostsListDto> posts = postsSearchService.findByUser(user1, 2, 2);

        // 5. expectedTotalElements = user1이 작성한 게시글 개수로 해서 결과 검증
        assertPageForServiceTest(posts, expectedTotalElements);
    }
}
