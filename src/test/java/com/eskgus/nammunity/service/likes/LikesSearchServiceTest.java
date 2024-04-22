package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.converter.LikesConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.helper.PaginationTestHelper;
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

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;
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

        testDB.savePostLikes(post1Id, user1);
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
        callAndAssertFindLikesByUser(5, likesRepository::findByUser);

        // 2. findPostLikesByUser()
        callAndAssertFindLikesByUser(2, likesRepository::findPostLikesByUser);

        // 3. findCommentLikesByUser()
        callAndAssertFindLikesByUser(2, likesRepository::findCommentLikesByUser);
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

    private void callAndAssertFindLikesByUser(int size, BiFunction<User, Pageable, Page<LikesListDto>> finder) {
        int page = 1;
        User user = users[1];

        Page<LikesListDto> actualPage = likesSearchService.findLikesByUser(user, finder, page, size);
        Page<LikesListDto> expectedPage = createExpectedPage(page, size, user, finder);

        assertFindLikesByUser(actualPage, expectedPage);
    }

    private Page<LikesListDto> createExpectedPage(int page, int size, User user,
                                                  BiFunction<User, Pageable, Page<LikesListDto>> finder) {
        Pageable pageable = createPageable(page, size);
        return finder.apply(user, pageable);
    }

    private void assertFindLikesByUser(Page<LikesListDto> actualPage, Page<LikesListDto> expectedPage) {
        PaginationTestHelper<LikesListDto, Likes> paginationHelper
                = new PaginationTestHelper<>(actualPage, expectedPage, new LikesConverterForTest());
        paginationHelper.assertContents();
    }

    @Test
    public void existsByPostsAndUser() {
        // 1. user1이 post1 좋아요 후 호출
        callAndAssertExistsByContentsAndUser(post, users[0], true);

        // 2. user2가 post1 좋아요 x 후 호출
        callAndAssertExistsByContentsAndUser(post, users[1], false);
    }

    private <T> void callAndAssertExistsByContentsAndUser(T content, User user, boolean expectedDoesUserLikeContent) {
        boolean actualDoesUserLikeContent = callExistsByContentsAndUser(content, user);
        assertThat(actualDoesUserLikeContent).isEqualTo(expectedDoesUserLikeContent);
    }

    private <T> boolean callExistsByContentsAndUser(T content, User user) {
        if (content instanceof Posts) {
            return likesSearchService.existsByPostsAndUser((Posts) content, user);
        }
        return likesSearchService.existsByCommentsAndUser((Comments) content, user);
    }

    @Test
    public void existsByCommentsAndUser() {
        // 1. user1이 comment1 좋아요 후 호출
        callAndAssertExistsByContentsAndUser(comment, users[0], true);

        // 2. user2가 comment1 좋아요 x 후 호출
        callAndAssertExistsByContentsAndUser(comment, users[1], false);
    }
}
