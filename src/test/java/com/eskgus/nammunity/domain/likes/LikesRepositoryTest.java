package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.util.FinderUtil;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.eskgus.nammunity.util.FinderUtil.callAndAssertFindContentsByUser;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class LikesRepositoryTest {
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
    public void countByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user1 = userRepository.findById(1L).get();
        User user2 = userRepository.findById(testDB.signUp(2L, Role.USER)).get();

        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        // 4. user2가 좋아요 x 후 호출
        callAndAssertCountLikesByUser(user2, likesRepository::countByUser, 0);

        // 5. user1이 게시글 좋아요 + 댓글 좋아요 후 호출
        callAndAssertCountLikesByUser(user1, likesRepository::countByUser, likesRepository.count());
    }

    @Test
    public void countPostLikesByUser() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        // 4. user1이 게시글 좋아요 + 댓글 좋아요 후 호출, 리턴 값이 1(게시글 좋아요 1개)인지 확인
        callAndAssertCountLikesByUser(user1, likesRepository::countPostLikesByUser, 1);
    }

    @Test
    public void countCommentLikesByUser() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        // 4. user1이 게시글 좋아요 + 댓글 좋아요 후 호출, 리턴 값이 1(댓글 좋아요 1개)인지 확인
        callAndAssertCountLikesByUser(user1, likesRepository::countCommentLikesByUser, 1);
    }

    @Test
    public void findByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user = userRepository.findById(testDB.signUp(2L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isEqualTo(2);

        // 2. expectedIdList 가져오기
        List<Long> expectedIdList = getExpectedIdList("post comment", user);

        // 3. user2로 findByUser() 호출 + 검증
        FinderUtil.FindDto<Likes> findDto = FinderUtil.FindDto.<Likes>builder()
                .user(user).finder(likesRepository::findByUser).expectedIdList(expectedIdList).build();
        callAndAssertFindContentsByUser(findDto);
    }

    @Test
    public void findPostLikesByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user = userRepository.findById(testDB.signUp(2L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isEqualTo(2);

        // 2. expectedIdList 가져오기
        List<Long> expectedIdList = getExpectedIdList("post", user);

        // 3. user2로 findPostLikesByUser() 호출 + 검증
        FinderUtil.FindDto<Likes> findDto = FinderUtil.FindDto.<Likes>builder()
                .user(user).finder(likesRepository::findPostLikesByUser).expectedIdList(expectedIdList).build();
        callAndAssertFindContentsByUser(findDto);
    }

    @Test
    public void findCommentLikesByUser() {
        // 1. user1 회원가입 + user2 회원가입
        User user = userRepository.findById(testDB.signUp(2L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isEqualTo(2);

        // 2. expectedIdList 가져오기
        List<Long> expectedIdList = getExpectedIdList("comment", user);

        // 3. user2로 findCommentLikesByUser() 호출 + 검증
        FinderUtil.FindDto<Likes> findDto = FinderUtil.FindDto.<Likes>builder()
                .user(user).finder(likesRepository::findCommentLikesByUser).expectedIdList(expectedIdList).build();
        callAndAssertFindContentsByUser(findDto);
    }

    @Test
    public void deleteByPosts() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성 * 2
        Posts post2 = postsRepository.findById(testDB.savePosts(user1)).get();
        Assertions.assertThat(postsRepository.count()).isEqualTo(2);

        // 3. user1, post2로 deleteByPosts() 호출 + 검증
        callAndAssertDeleteByField(testDB::savePostLikes, likesRepository::deleteByPosts, post2, user1);
    }

    @Test
    public void deleteByComments() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성 + 댓글 작성 * 2
        Posts post1 = postsRepository.findById(1L).get();
        Comments comment2 = commentsRepository.findById(testDB.saveComments(post1.getId(), user1)).get();
        Assertions.assertThat(commentsRepository.count()).isEqualTo(2);

        // 3. user1, comment2로 deleteByComments() 호출 + 검증
        callAndAssertDeleteByField(testDB::saveCommentLikes, likesRepository::deleteByComments, comment2, user1);
    }

    private void callAndAssertCountLikesByUser(User user, Function<User, Long> function, long expectedCount) {
        // 1. user로 function 호출
        long actualCount = function.apply(user);

        // 2. 리턴 값이 expectedCount랑 같은지 확인
        Assertions.assertThat(actualCount).isEqualTo(expectedCount);
    }

    private List<Long> getExpectedIdList(String content, User user) {
        // 1. user1이 게시글 작성 + user2가 게시글 작성
        Posts post1 = postsRepository.findById(1L).get();
        Posts post2 = postsRepository.findById(testDB.savePosts(user)).get();
        Assertions.assertThat(postsRepository.count()).isEqualTo(2);

        // 2. user1이 댓글 작성 * 2
        Comments comment1 = commentsRepository.findById(1L).get();
        Comments comment2 = commentsRepository.findById(testDB.saveComments(post2.getId(), user)).get();
        Assertions.assertThat(commentsRepository.count()).isEqualTo(2);

        // 3. user1이 게시글 좋아요 + 댓글 좋아요
        // 4. user2가 게시글 좋아요 * 2 + 댓글 좋아요 * 2
        List<Posts> posts = Arrays.asList(post1, post2);
        List<Comments> comments = Arrays.asList(comment1, comment2);
        List<Long> expectedIdList = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            Long postLikeId = testDB.savePostLikes(posts.get(i).getId(), user);
            Long commentLikeId = testDB.saveCommentLikes(comments.get(i).getId(), user);

            // 5. List<Long> expectedIdList에 user2의 댓글 좋아요 id 내림차순 저장
            if (content.contains("post")) {
                expectedIdList.add(postLikeId);
            }
            if (content.contains("comment")){
                expectedIdList.add(commentLikeId);
            }
        }
        Assertions.assertThat(likesRepository.count()).isEqualTo(6);

        return expectedIdList;
    }

    private <T> void callAndAssertDeleteByField(BiFunction<Long, User, Long> likesSaver,
                                                BiConsumer<T, User> likesDeleter,
                                                T content, User user) {
        // 1. 들어온 content의 id 구하기
        long contentId;
        if (content instanceof Posts) {
            contentId = ((Posts) content).getId();
        } else {
            contentId = ((Comments) content).getId();
        }

        // 2. user가 post/comment 좋아요 (* 2)
        Long likeId = likesSaver.apply(contentId, user);

        // 3. post/comment, user로 deleteBy@@() 호출
        likesDeleter.accept(content, user);

        // 4. user의 post/comment 좋아요가 db에 존재하지 않는지 확인
        Optional<Likes> result = likesRepository.findById(likeId);
        Assertions.assertThat(result).isNotPresent();
    }
}
