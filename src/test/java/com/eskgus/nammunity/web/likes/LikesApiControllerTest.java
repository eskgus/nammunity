package com.eskgus.nammunity.web.likes;

import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LikesApiControllerTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private LikesRepository likesRepository;

    private User user;
    private Posts post;
    private Comments comment;

    @BeforeEach
    public void setUp() {
        Long userId = testDB.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);

        Long postId = testDB.savePosts(user);
        this.post = assertOptionalAndGetEntity(postsRepository::findById, postId);

        Long commentId = testDB.saveComments(postId, user);
        this.comment = assertOptionalAndGetEntity(commentsRepository::findById, commentId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDB.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveLikes() throws Exception {
        // 일반 1. 게시글 좋아요
        mockMvcTestHelper.requestAndAssertStatusIsOkWithParam(
                post("/api/likes"), "postsId", post.getId());

        // 일반 2. 댓글 좋아요
        mockMvcTestHelper.requestAndAssertStatusIsOkWithParam(
                post("/api/likes"), "commentsId", comment.getId());
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteLikes() throws Exception {
        // 일반 1. 게시글 좋아요 취소
        saveLike(testDB::savePostLikes, post.getId());
        mockMvcTestHelper.requestAndAssertStatusIsOkWithParam(
                delete("/api/likes"), "postsId", post.getId());

        // 일반 2. 댓글 좋아요 취소
        saveLike(testDB::saveCommentLikes, comment.getId());
        mockMvcTestHelper.requestAndAssertStatusIsOkWithParam(
                delete("/api/likes"), "commentsId", comment.getId());
    }

    private Long saveLike(BiFunction<Long, User, Long> saver, Long contentId) {
        Long likeId = saver.apply(contentId, user);
        assertOptionalAndGetEntity(likesRepository::findById, likeId);
        return likeId;
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedLikes() throws Exception {
        List<Long> requestDto = createLikeIds();

        mockMvcTestHelper.requestAndAssertStatusIsOk(delete("/api/likes/selected-delete"), requestDto);
    }

    private List<Long> createLikeIds() {
        List<Long> requestDto = new ArrayList<>();

        Long postLikeId = saveLike(testDB::savePostLikes, post.getId());
        requestDto.add(postLikeId);

        Long commentLikeId = saveLike(testDB::saveCommentLikes, comment.getId());
        requestDto.add(commentLikeId);

        return requestDto;
    }
}
