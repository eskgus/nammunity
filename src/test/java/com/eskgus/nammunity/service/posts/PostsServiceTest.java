package com.eskgus.nammunity.service.posts;

import com.eskgus.nammunity.converter.CommentsConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.ReasonsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.FindHelperForTest2;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.posts.PostWithReasonsDto;
import com.eskgus.nammunity.web.dto.posts.PostsReadDto;
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
    private CommentsSearchService commentsSearchService;

    private User[] users;
    private Posts post;
    private final int page = 1;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        Long user2Id = testDB.signUp(2L, Role.USER);
        assertThat(userRepository.count()).isEqualTo(user2Id);

        User user1 = userRepository.findById(user1Id).get();
        User user2 = userRepository.findById(user2Id).get();

        this.users = new User[]{ user1, user2 };

        Long postId = testDB.savePosts(user1);
        assertThat(postsRepository.count()).isEqualTo(postId);

        this.post = postsRepository.findById(postId).get();
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
        Principal principal = createPrincipalWithUser1();
        return postsService.readPosts(post.getId(), principal);
    }

    private Principal createPrincipalWithUser1() {
        return () -> users[0].getUsername();
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
        ContentsPageDto<CommentsReadDto> actualResult = callReadCommentsAndGetActualResult();
        Page<CommentsReadDto> expectedContents = commentsSearchService.findByPosts(post, users[0], page);

        FindHelperForTest2<CommentsReadDto, Comments> findHelper = FindHelperForTest2.<CommentsReadDto, Comments>builder()
                    .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(new CommentsConverterForTest<>(CommentsReadDto.class)).build();
        findHelper.callAndAssertFind();
    }

    private ContentsPageDto<CommentsReadDto> callReadCommentsAndGetActualResult() {
        Principal principal = createPrincipalWithUser1();
        return postsService.readComments(post.getId(), principal, page);
    }
}
