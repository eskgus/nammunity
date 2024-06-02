package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.converter.LikesConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.PaginationTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
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

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class LikesServiceTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private LikesRepository likesRepository;

    @Autowired
    private LikesService likesService;

    private User[] users;
    private Posts post;
    private Comments comment;
    private Long commentLikeId;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDataHelper.signUp(1L, Role.USER);
        Long user2Id = testDataHelper.signUp(2L, Role.USER);

        User user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);
        User user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        this.users = new User[]{ user1, user2 };

        Long postId = testDataHelper.savePosts(user1);
        this.post = assertOptionalAndGetEntity(postsRepository::findById, postId);

        Long commentId = testDataHelper.saveComments(postId, user1);
        this.comment = assertOptionalAndGetEntity(commentsRepository::findById, commentId);

        Long postLikeId = testDataHelper.savePostLikes(postId, user1);
        assertOptionalAndGetEntity(likesRepository::findById, postLikeId);

        this.commentLikeId = testDataHelper.saveCommentLikes(commentId, user1);
        assertOptionalAndGetEntity(likesRepository::findById, commentLikeId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void save() {
        // 1. 게시글 좋아요
        callAndAssertSave(post.getId(), ContentType.POSTS);

        // 2. 댓글 좋아요
        callAndAssertSave(comment.getId(), ContentType.COMMENTS);
    }

    private void callAndAssertSave(Long contentId, ContentType contentType) {
        User user = users[0];
        Principal principal = user::getUsername;

        Long postId = contentType.equals(ContentType.POSTS) ? contentId : null;
        Long commentId = contentType.equals(ContentType.COMMENTS) ? contentId : null;
        Long id = likesService.save(postId, commentId, principal);

        Likes like = assertOptionalAndGetEntity(likesRepository::findById, id);
        assertSavedLike(contentId, contentType, user, like);
    }

    private void assertSavedLike(Long contentId, ContentType contentType, User user, Likes like) {
        Long actualContentId = contentType.equals(ContentType.POSTS)
                ? like.getPosts().getId() : like.getComments().getId();

        assertThat(actualContentId).isEqualTo(contentId);
        assertThat(like.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    public void deleteByContentId() {
        // 1. 게시글 좋아요 취소
        callAndAssertDeleteByContentId(post.getId(), ContentType.POSTS);

        // 2. 댓글 좋아요 취소
        callAndAssertDeleteByContentId(comment.getId(), ContentType.COMMENTS);
    }

    private void callAndAssertDeleteByContentId(Long contentId, ContentType contentType) {
        User user = users[0];

        Long likeId = saveLike(contentId, contentType, user);

        deleteLike(contentId, contentType, user);

        assertDeletedLike(likeId);
    }

    private Long saveLike(Long contentId, ContentType contentType, User user) {
        Long likeId = contentType.equals(ContentType.POSTS)
                ? testDataHelper.savePostLikes(contentId, user) : testDataHelper.saveCommentLikes(contentId, user);

        assertOptionalAndGetEntity(likesRepository::findById, likeId);
        return likeId;
    }

    private void deleteLike(Long contentId, ContentType contentType, User user) {
        Principal principal = user::getUsername;

        Long postId = contentType.equals(ContentType.POSTS) ? contentId : null;
        Long commentId = contentType.equals(ContentType.COMMENTS) ? contentId : null;

        likesService.deleteByContentId(postId, commentId, principal);
    }

    private void assertDeletedLike(Long likeId) {
        Optional<Likes> result = likesRepository.findById(likeId);
        assertThat(result).isNotPresent();
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
            testDataHelper.savePostLikes(posts.get(i).getId(), users[1]);
            testDataHelper.saveCommentLikes(comments.get(i).getId(), users[1]);
        }
        assertThat(likesRepository.count()).isEqualTo(posts.size() + comments.size() + commentLikeId);
    }

    private Posts savePost() {
        Long postId = testDataHelper.savePosts(users[0]);
        return assertOptionalAndGetEntity(postsRepository::findById, postId);
    }

    private Comments saveComment() {
        Long commentId = testDataHelper.saveComments(post.getId(), users[0]);
        return assertOptionalAndGetEntity(commentsRepository::findById, commentId);
    }

    private void callAndAssertFindLikesByUser(int size, BiFunction<User, Pageable, Page<LikesListDto>> finder) {
        int page = 1;
        User user = users[1];

        Page<LikesListDto> actualPage = likesService.findLikesByUser(user, finder, page, size);
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
            return likesService.existsByPostsAndUser((Posts) content, user);
        }
        return likesService.existsByCommentsAndUser((Comments) content, user);
    }

    @Test
    public void existsByCommentsAndUser() {
        // 1. user1이 comment1 좋아요 후 호출
        callAndAssertExistsByContentsAndUser(comment, users[0], true);

        // 2. user2가 comment1 좋아요 x 후 호출
        callAndAssertExistsByContentsAndUser(comment, users[1], false);
    }
}
