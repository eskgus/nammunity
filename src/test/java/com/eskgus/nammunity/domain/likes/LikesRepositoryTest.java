package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.converter.LikesConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.helper.FindHelperForTest;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.helper.repository.finder.RepositoryBiFinderForTest;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.eskgus.nammunity.util.FindUtilForTest.callAndAssertFind;
import static com.eskgus.nammunity.util.FindUtilForTest.initializeFindHelper;
import static org.assertj.core.api.Assertions.assertThat;

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

    private User[] users;
    private Posts post1;
    private Likes post1Like;
    private Likes comment1Like;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        Long user2Id = testDB.signUp(2L, Role.USER);
        assertThat(userRepository.count()).isEqualTo(user2Id);

        User user1 = userRepository.findById(user1Id).get();
        User user2 = userRepository.findById(user2Id).get();

        this.users = new User[]{ user1, user2 };

        Long post1Id = testDB.savePosts(user1);
        assertThat(postsRepository.count()).isEqualTo(post1Id);

        this.post1 = postsRepository.findById(post1Id).get();

        Long comment1Id = testDB.saveComments(post1Id, user1);
        assertThat(commentsRepository.count()).isEqualTo(comment1Id);

        Long post1LikeId = testDB.savePostLikes(post1Id, user1);
        Long comment1LikeId = testDB.saveCommentLikes(comment1Id, user1);
        assertThat(likesRepository.count()).isEqualTo(comment1LikeId);

        this.post1Like = likesRepository.findById(post1LikeId).get();
        this.comment1Like = likesRepository.findById(comment1LikeId).get();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void countByUser() {
        // 1. user2가 좋아요 x 후 호출
        callAndAssertCountLikesByUser(users[1], likesRepository::countByUser, 0);

        // 2. user1이 게시글 좋아요 + 댓글 좋아요 후 호출
        callAndAssertCountLikesByUser(users[0], likesRepository::countByUser, comment1Like.getId());
    }

    private void callAndAssertCountLikesByUser(User user, Function<User, Long> counter, long expectedCount) {
        long actualCount = counter.apply(user);
        Assertions.assertThat(actualCount).isEqualTo(expectedCount);
    }

    @Test
    public void countPostLikesByUser() {
        callAndAssertCountLikesByUser(users[0], likesRepository::countPostLikesByUser, post1Like.getId());
    }

    @Test
    public void countCommentLikesByUser() {
        callAndAssertCountLikesByUser(users[0], likesRepository::countCommentLikesByUser,
                comment1Like.getId() - post1Like.getId());
    }

    @Test
    public void findByUser() {
        callAndAssertFindLikesByUser(likesRepository::findByUser, null);
    }

    private void callAndAssertFindLikesByUser(RepositoryBiFinderForTest<LikesListDto, User> finder,
                                              ContentType contentType) {
        saveLikes();

        FindHelperForTest<RepositoryBiFinderForTest<LikesListDto, User>, Likes, LikesListDto, User> findHelper =
                createBiFindHelper(finder, contentType);
        callAndAssertFindLikes(findHelper);
    }

    private void saveLikes() {
        List<Posts> posts = new ArrayList<>();
        List<Comments> comments = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            posts.add(savePost());
            comments.add(saveComment());
        }

        for (int i = 0; i < posts.size(); i++) {
            testDB.savePostLikes(posts.get(i).getId(), users[1]);
            testDB.saveCommentLikes(comments.get(i).getId(), users[1]);
        }
        assertThat(likesRepository.count()).isEqualTo(posts.size() + comments.size() + comment1Like.getId());
    }

    private Posts savePost() {
        Long postId = testDB.savePosts(users[0]);
        assertThat(postsRepository.count()).isEqualTo(postId);

        return postsRepository.findById(postId).get();
    }

    private Comments saveComment() {
        Long commentId = testDB.saveComments(post1.getId(), users[0]);
        assertThat(commentsRepository.count()).isEqualTo(commentId);

        return commentsRepository.findById(commentId).get();
    }

    private FindHelperForTest<RepositoryBiFinderForTest<LikesListDto, User>, Likes, LikesListDto, User>
        createBiFindHelper(RepositoryBiFinderForTest<LikesListDto, User> finder,
                           ContentType contentType) {
        EntityConverterForTest<Likes, LikesListDto> entityConverter = new LikesConverterForTest();
        return FindHelperForTest.<RepositoryBiFinderForTest<LikesListDto, User>, Likes, LikesListDto, User>builder()
                .finder(finder)
                .contents(users[0])
                .contentType(contentType)
                .entityStream(likesRepository.findAll().stream())
                .page(1).limit(3)
                .entityConverter(entityConverter).build();
    }

    private void callAndAssertFindLikes(FindHelperForTest<RepositoryBiFinderForTest<LikesListDto, User>, Likes, LikesListDto, User>
                                                findHelper) {
        initializeFindHelper(findHelper);
        callAndAssertFind();
    }

    @Test
    public void findPostLikesByUser() {
        callAndAssertFindLikesByUser(likesRepository::findPostLikesByUser, ContentType.POSTS);
    }

    @Test
    public void findCommentLikesByUser() {
        callAndAssertFindLikesByUser(likesRepository::findCommentLikesByUser, ContentType.COMMENTS);
    }

    @Test
    public void deleteByPosts() {
        Posts post2 = savePost();

        callAndAssertDeleteByField(testDB::savePostLikes, likesRepository::deleteByPosts, post2);
    }

    private <T> void callAndAssertDeleteByField(BiFunction<Long, User, Long> likesSaver,
                                                BiConsumer<T, User> likesDeleter,
                                                T content) {
        Long contentId = getContentId(content);

        Long likeId = likesSaver.apply(contentId, users[0]);
        likesDeleter.accept(content, users[0]);
        assertDeleteByField(likeId);
    }

    private <T> Long getContentId(T content) {
        if (content instanceof Posts) {
            return ((Posts) content).getId();
        } else {
            return  ((Comments) content).getId();
        }
    }

    private void assertDeleteByField(Long likeId) {
        Optional<Likes> result = likesRepository.findById(likeId);
        Assertions.assertThat(result).isNotPresent();
    }

    @Test
    public void deleteByComments() {
        Comments comment2 = saveComment();

        callAndAssertDeleteByField(testDB::saveCommentLikes, likesRepository::deleteByComments, comment2);
    }
}
