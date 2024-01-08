package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.function.BiFunction;

import static com.eskgus.nammunity.util.FinderUtil.assertPageForServiceTest;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class LikesSearchServiceTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private LikesRepository likesRepository;

    @Autowired
    private LikesSearchService likesSearchService;

    @BeforeEach
    public void setUp() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isOne();

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 3. user1이 댓글 작성
        Long commentId = testDB.saveComments(postId, user1);
        Assertions.assertThat(commentsRepository.count()).isOne();

        // 4. user1이 게시글 좋아요 + 댓글 좋아요
        testDB.savePostLikes(postId, user1);
        Long latestLikeId = testDB.saveCommentLikes(commentId, user1);
        Assertions.assertThat(likesRepository.count()).isEqualTo(latestLikeId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void findLikesByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user2 = userRepository.findById(testDB.signUp(2L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isEqualTo(2);

        // 2. user1이 게시글 작성
        Posts post = postsRepository.findById(1L).get();

        // 3. user1이 댓글 작성
        Comments comment = commentsRepository.findById(1L).get();

        // 4. user1이 게시글 좋아요 * 1 + 댓글 좋아요 * 1
        // 5. user2가 게시글 좋아요 * 3 + 댓글 좋아요 * 3
        long expectedTotalElements1 = 0;
        for (int i = 0; i < 3; i++) {
            testDB.savePostLikes(post.getId(), user2);
            testDB.saveCommentLikes(comment.getId(), user2);
            expectedTotalElements1++;
        }
        long expectedTotalElements2 = expectedTotalElements1 * 2;

        // 6. findLikesByUser() 호출
        // 6-1. user = user2, page = 2, size = 2로 해서 findPostLikesByUser() 호출 + 검증
        callAndAssertFindLikesByUser(likesRepository::findPostLikesByUser, 2, expectedTotalElements1);

        // 6-2. user = user2, page = 2, size = 2로 해서 findCommentLikesByUser() 호출 + 검증
        callAndAssertFindLikesByUser(likesRepository::findCommentLikesByUser, 2, expectedTotalElements1);

        // 6-3. user = user2, page = 2, size = 5로 해서 findByUser() 호출 + 검증
        callAndAssertFindLikesByUser(likesRepository::findByUser, 5, expectedTotalElements2);
    }

    @Test
    public void countLikesByUser() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        // 4. user1이 게시글 좋아요 + 댓글 좋아요
        // 5. countLikesByUser() 호출
        // 5-1. 함수형 인터페이스로 countByUser() 넣어서 호출
        long count1 = likesSearchService.countLikesByUser(user1, likesRepository::countByUser);
        Assertions.assertThat(count1).isEqualTo(likesRepository.count());

        // 5-2. 함수형 인터페이스로 countPostLikesByUser() 넣어서 호출
        long count2 = likesSearchService.countLikesByUser(user1, likesRepository::countPostLikesByUser);
        Assertions.assertThat(count2).isEqualTo(1);

        // 5-3. 함수형 인터페이스로 countCommentLikesByUser() 넣어서 호출
        long count3 = likesSearchService.countLikesByUser(user1, likesRepository::countCommentLikesByUser);
        Assertions.assertThat(count3).isEqualTo(1);
    }

    private void callAndAssertFindLikesByUser(BiFunction<User, Pageable, Page<LikesListDto>> finder,
                                              int size, long expectedTotalElements) {
        User user2 = userRepository.findById(2L).get();

        // 1. findLikesByUser() 호출
        Page<LikesListDto> likes = likesSearchService.findLikesByUser(user2, finder, 2, size);

        // 2. 결과 검증
        assertPageForServiceTest(likes, expectedTotalElements);
    }
}
