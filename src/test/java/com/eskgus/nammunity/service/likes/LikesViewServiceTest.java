package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.converter.LikesConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.helper.ContentsPageDtoTestHelper;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class LikesViewServiceTest {
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
    private LikesViewService likesViewService;

    @Autowired
    private LikesService likesService;

    private User[] users;
    private Posts post;
    private Comments comment;
    private Long commentLikeId;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        Long user2Id = testDB.signUp(2L, Role.USER);

        User user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);
        User user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        this.users = new User[]{ user1, user2 };

        Long postId = testDB.savePosts(user1);
        this.post = assertOptionalAndGetEntity(postsRepository::findById, postId);

        Long commentId = testDB.saveComments(postId, user1);
        this.comment = assertOptionalAndGetEntity(commentsRepository::findById, commentId);

        Long postLikeId = testDB.savePostLikes(postId, user1);
        assertOptionalAndGetEntity(likesRepository::findById, postLikeId);

        this.commentLikeId = testDB.saveCommentLikes(commentId, user1);
        assertOptionalAndGetEntity(likesRepository::findById, commentLikeId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDB.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void listLikes() {
        User user = users[1];
        saveLikes(user);

        // 1. listLikes()
        callAndAssertListLikes(user, likesRepository::findByUser);

        // 2. listPostLikes()
        callAndAssertListLikes(user, likesRepository::findPostLikesByUser);

        // 3. listCommentLikes()
        callAndAssertListLikes(user, likesRepository::findCommentLikesByUser);
    }

    private void saveLikes(User user) {
        List<Posts> posts = savePosts(user);
        List<Comments> comments = saveComments(user);

        for (int i = 0; i < posts.size(); i++) {
            testDB.savePostLikes(posts.get(i).getId(), user);
            testDB.saveCommentLikes(comments.get(i).getId(), user);
        }
        assertThat(likesRepository.count()).isEqualTo(posts.size() + comments.size() + commentLikeId);
    }

    private List<Posts> savePosts(User user) {
        List<Posts> posts = new ArrayList<>();
        posts.add(post);

        for (int i = 0; i < 30; i++) {
            posts.add(savePost(user));
        }
        return posts;
    }

    private Posts savePost(User user) {
        Long postId = testDB.savePosts(user);
        return assertOptionalAndGetEntity(postsRepository::findById, postId);
    }

    private List<Comments> saveComments(User user) {
        List<Comments> comments = new ArrayList<>();
        comments.add(comment);

        for (int i = 0; i < 30; i++) {
            comments.add(saveComment(user));
        }
        return comments;
    }

    private Comments saveComment(User user) {
        Long commentId = testDB.saveComments(post.getId(), user);
        return assertOptionalAndGetEntity(commentsRepository::findById, commentId);
    }

    private void callAndAssertListLikes(User user, BiFunction<User, Pageable, Page<LikesListDto>> finder) {
        int page = 1;
        ContentsPageDto<LikesListDto> actualResult = callListLikesAndGetActualResult(user, finder, page);
        Page<LikesListDto> expectedContents = likesService.findLikesByUser(user, finder, page, 20);

        ContentsPageDtoTestHelper<LikesListDto, Likes> findHelper = ContentsPageDtoTestHelper.<LikesListDto, Likes>builder()
                .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(new LikesConverterForTest()).build();
        findHelper.createExpectedResultAndAssertContentsPage();
    }

    private ContentsPageDto<LikesListDto> callListLikesAndGetActualResult(User user,
                                                                          BiFunction<User, Pageable, Page<LikesListDto>> finder,
                                                                          int page) {
        Principal principal = user::getUsername;
        return likesViewService.listLikes(finder, principal, page);
    }
}
