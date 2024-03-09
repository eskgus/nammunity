package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.converter.LikesConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.helper.FindHelperForTest;
import com.eskgus.nammunity.helper.enums.ContentTypeForTest;
import com.eskgus.nammunity.helper.repository.ServiceQuadFinderForTest;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
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
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.eskgus.nammunity.util.FindUtilForTest.callAndAssertFind;
import static com.eskgus.nammunity.util.FindUtilForTest.initializeFindHelper;
import static org.assertj.core.api.Assertions.assertThat;

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

    private User[] users;
    private Posts post;
    private Comments comment;
    private Long postLikeId;
    private Long commentLikeId;

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

        this.post = postsRepository.findById(post1Id).get();

        Long comment1Id = testDB.saveComments(post1Id, user1);
        assertThat(commentsRepository.count()).isEqualTo(comment1Id);

        this.comment = commentsRepository.findById(comment1Id).get();

        this.postLikeId = testDB.savePostLikes(post1Id, user1);
        this.commentLikeId = testDB.saveCommentLikes(comment1Id, user1);
        assertThat(likesRepository.count()).isEqualTo(commentLikeId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void findLikesByUser() {
        saveLikesWithUser2();

        // 1. findByUser()
        FindHelperForTest<ServiceQuadFinderForTest<LikesListDto>, Likes, LikesListDto> findHelper1
                = createFindHelper(5, null, likesRepository::findByUser);
        callAndAssertFindLikesByUser(findHelper1);

        // 2. findPostLikesByUser()
        FindHelperForTest<ServiceQuadFinderForTest<LikesListDto>, Likes, LikesListDto> findHelper2
                = createFindHelper(2, ContentTypeForTest.POSTS, likesRepository::findPostLikesByUser);
        callAndAssertFindLikesByUser(findHelper2);

        // 3. findCommentLikesByUser()
        FindHelperForTest<ServiceQuadFinderForTest<LikesListDto>, Likes, LikesListDto> findHelper3
                = createFindHelper(2, ContentTypeForTest.COMMENTS, likesRepository::findCommentLikesByUser);
        callAndAssertFindLikesByUser(findHelper3);
    }

    private void saveLikesWithUser2() {
        List<Posts> posts = new ArrayList<>();
        List<Comments> comments = new ArrayList<>();
        posts.add(post);
        comments.add(comment);
        for (int i = 0; i < 2; i++) {
            posts.add(savePost());
            comments.add(saveComment());
        }

        for (int i = 0; i < posts.size(); i++) {
            testDB.savePostLikes(posts.get(i).getId(), users[1]);
            testDB.saveCommentLikes(comments.get(i).getId(), users[1]);
        }
        assertThat(likesRepository.count()).isEqualTo(posts.size() + comments.size() + commentLikeId);
    }

    private Posts savePost() {
        Long postId = testDB.savePosts(users[0]);
        assertThat(postsRepository.count()).isEqualTo(postId);

        return postsRepository.findById(postId).get();
    }

    private Comments saveComment() {
        Long commentId = testDB.saveComments(post.getId(), users[0]);
        assertThat(commentsRepository.count()).isEqualTo(commentId);

        return commentsRepository.findById(commentId).get();
    }

    private FindHelperForTest<ServiceQuadFinderForTest<LikesListDto>, Likes, LikesListDto>
    createFindHelper(int limit,
                     ContentTypeForTest contentTypeOfLikes,
                     BiFunction<User, Pageable, Page<LikesListDto>> likesFinder) {
        EntityConverterForTest<Likes, LikesListDto> entityConverter = new LikesConverterForTest();
        return FindHelperForTest.<ServiceQuadFinderForTest<LikesListDto>, Likes, LikesListDto>builder()
                .finder(likesSearchService::findLikesByUser).user(users[1])
                .entityStream(likesRepository.findAll().stream())
                .contentTypeOfLikes(contentTypeOfLikes)
                .page(2).limit(limit)
                .entityConverter(entityConverter)
                .likesFinder(likesFinder).build();
    }

    private void callAndAssertFindLikesByUser(FindHelperForTest<ServiceQuadFinderForTest<LikesListDto>,
            Likes, LikesListDto> findHelper) {
        initializeFindHelper(findHelper);
        callAndAssertFind();
    }

    @Test
    public void countLikesByUser() {
        // 1. countByUser()
        callAndAssertCountLikesByUser(likesRepository::countByUser, commentLikeId);

        // 2.countPostLikesByUser()
        callAndAssertCountLikesByUser(likesRepository::countPostLikesByUser, postLikeId);

        // 3. countCommentLikesByUser()
        callAndAssertCountLikesByUser(likesRepository::countCommentLikesByUser, commentLikeId - postLikeId);
    }

    private void callAndAssertCountLikesByUser(Function<User, Long> counter, Long expectedCount) {
        Long actualCount = counter.apply(users[0]);
        assertThat(actualCount).isEqualTo(expectedCount);
    }
}
