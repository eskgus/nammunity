package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.converter.CommentsConverterForTest;
import com.eskgus.nammunity.converter.PostsConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.ReasonsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.ContentsPageDtoTestHelper;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostWithReasonsDto;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.eskgus.nammunity.web.dto.posts.PostsReadDto;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.reports.ReasonsListDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PostsServiceTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private ReasonsRepository reasonsRepository;

    @Autowired
    private PostsService postsService;

    @Autowired
    private PostsSearchService postsSearchService;

    @Autowired
    private CommentsSearchService commentsSearchService;

    private User[] users;
    private Posts post;
    private final int page = 1;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        User user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDB.signUp(2L, Role.USER);
        User user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        this.users = new User[]{ user1, user2 };

        Long postId = testDB.savePosts(user1);
        this.post = assertOptionalAndGetEntity(postsRepository::findById, postId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDB.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void readPosts() {
        PostWithReasonsDto postWithReasonsDto = callReadPostsAndGetPostWithReasonsDto();
        assertPostWithReasonsDto(postWithReasonsDto);
    }

    private PostWithReasonsDto callReadPostsAndGetPostWithReasonsDto() {
        Principal principal = createPrincipal(users[0]);
        return postsService.readPosts(post.getId(), principal);
    }

    private Principal createPrincipal(User user) {
        return user::getUsername;
    }

    private void assertPostWithReasonsDto(PostWithReasonsDto postWithReasonsDto) {
        assertPostReadDto(postWithReasonsDto.getPost());
        assertReasons(postWithReasonsDto.getReasons());
    }

    private void assertPostReadDto(PostsReadDto postsReadDto) {
        assertThat(postsReadDto.getId()).isEqualTo(post.getId());
        assertThat(postsReadDto.isDoesUserWritePost()).isEqualTo(true); // user1이 post 작성
        assertThat(postsReadDto.isDoesUserLikePost()).isEqualTo(false); // 좋아요 저장 x
    }

    private void assertReasons(List<ReasonsListDto> reasons) {
        for (long i = 1; i < reasonsRepository.count(); i++) {
            ReasonsListDto reason = reasons.get((int) (i - 1));
            assertThat(reason.getId()).isEqualTo(i);
        }
    }

    @Test
    public void readComments() {
        saveComments();

        callAndAssertReadCommentS();
    }

    private void saveComments() {
        int numberOfCommentsByUser = 15;
        for (int i = 0; i < numberOfCommentsByUser; i++) {
            for (User user : users) {
                testDB.saveComments(post.getId(), user);
            }
        }
        assertThat(commentsRepository.count()).isEqualTo((long) numberOfCommentsByUser * users.length);
    }

    private void callAndAssertReadCommentS() {
        User user = users[0];
        ContentsPageDto<CommentsReadDto> actualResult = callReadCommentsAndGetActualResult(user);
        Page<CommentsReadDto> expectedContents = commentsSearchService.findByPosts(post, user, page);

        ContentsPageDtoTestHelper<CommentsReadDto, Comments> findHelper = ContentsPageDtoTestHelper.<CommentsReadDto, Comments>builder()
                    .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(new CommentsConverterForTest<>(CommentsReadDto.class)).build();
        findHelper.createExpectedResultAndAssertContentsPage();
    }

    private ContentsPageDto<CommentsReadDto> callReadCommentsAndGetActualResult(User user) {
        Principal principal = createPrincipal(user);
        return postsService.readComments(post.getId(), principal, page);
    }

    @Test
    public void listPosts() {
        savePosts();

        callAndAssertListPosts();
    }

    private void savePosts() {
        int numberOfPostsByUser = 30;
        for (int i = 0; i < numberOfPostsByUser; i++) {
            for (User user : users) {
                testDB.savePosts(user);
            }
        }
        assertThat(postsRepository.count()).isEqualTo((long) numberOfPostsByUser * users.length + post.getId());
    }

    private void callAndAssertListPosts() {
        User user = users[0];
        ContentsPageDto<PostsListDto> actualResult = callListPostsAndGetActualResult(user);
        Page<PostsListDto> expectedContents = postsSearchService.findByUser(user, page, 20);

        ContentsPageDtoTestHelper<PostsListDto, Posts> findHelper = ContentsPageDtoTestHelper.<PostsListDto, Posts>builder()
                .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(new PostsConverterForTest()).build();
        findHelper.createExpectedResultAndAssertContentsPage();
    }

    private ContentsPageDto<PostsListDto> callListPostsAndGetActualResult(User user) {
        Principal principal = createPrincipal(user);
        return postsService.listPosts(principal, page);
    }

    @Test
    public void save() {
        User user = users[0];

        PostsSaveDto requestDto = PostsSaveDto.builder().title("title").content("content").build();
        Principal principal = user::getUsername;

        Long id = postsService.save(requestDto, principal);

        Posts post = assertOptionalAndGetEntity(postsRepository::findById, id);
        assertSavedPost(requestDto, user, post);
    }

    private void assertSavedPost(PostsSaveDto requestDto, User user, Posts post) {
        assertThat(post.getTitle()).isEqualTo(requestDto.getTitle());
        assertThat(post.getContent()).isEqualTo(requestDto.getContent());
        assertThat(post.getUser().getId()).isEqualTo(user.getId());
    }
}
