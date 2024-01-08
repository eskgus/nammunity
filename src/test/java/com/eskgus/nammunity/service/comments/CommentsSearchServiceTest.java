package com.eskgus.nammunity.service.comments;

import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.eskgus.nammunity.util.FinderUtil.assertPageForServiceTest;

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

    @Test
    public void findByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        User user2 = userRepository.findById(testDB.signUp(2L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isEqualTo(2);

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(user1);

        // 3. user1이 댓글 작성 * 3
        long expectedTotalElements = 0;
        for (int i = 0; i < 3; i++) {
            expectedTotalElements = testDB.saveComments(postId, user1);
        }

        // 4. user2가 댓글 작성 * 1
        Long latestCommentId = testDB.saveComments(postId, user2);
        Assertions.assertThat(commentsRepository.count()).isEqualTo(latestCommentId);

        // 5. user = user1, page = 2, size = 2로 해서 findByUser() 호출
        Page<CommentsListDto> comments = commentsSearchService.findByUser(user1, 2, 2);

        // 6. expectedTotalElements = user1이 작성한 댓글 개수로 해서 결과 검증
        assertPageForServiceTest(comments, expectedTotalElements);
    }
}
