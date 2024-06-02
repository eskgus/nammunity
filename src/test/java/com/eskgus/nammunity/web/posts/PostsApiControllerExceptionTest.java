package com.eskgus.nammunity.web.posts;

import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerExceptionTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    private User user;
    private Posts post;

    private MockHttpServletRequestBuilder requestBuilder;
    private final String title = "title";
    private final String content = "content";

    @BeforeEach
    public void setUp() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    @WithMockUser(username = "username1")
    public void savePostsExceptions() throws Exception {
        this.requestBuilder = post("/api/posts");

        // 예외 1. 제목 입력 x
        requestAndAssertSavePostsExceptions("", content, title, "제목을 입력하세요.");

        // 예외 2. 내용 입력 x
        requestAndAssertSavePostsExceptions(title, "", content, "내용을 입력하세요.");

        // 예외 3. 제목 100글자 초과
        requestAndAssertSavePostsExceptions("t".repeat(101), content,
                title, "글 제목은 100글자 이하여야 합니다.");

        // 예외 4. 내용 3000글자 초과
        requestAndAssertSavePostsExceptions(title, "c".repeat(3001),
                content, "글 내용은 3000글자 이하여야 합니다.");
    }

    private void requestAndAssertSavePostsExceptions(String title, String content,
                                                     String expectedField, String expectedDefaultMessage) throws Exception {
        PostsSaveDto requestDto = createPostsSaveDto(title, content);
        ResultMatcher[] resultMatchers = mockMvcTestHelper.createResultMatchers(expectedField, expectedDefaultMessage);

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private PostsSaveDto createPostsSaveDto(String title, String content) {
        return PostsSaveDto.builder().title(title).content(content).build();
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePostsExceptions() throws Exception {
        this.post = savePost();
        this.requestBuilder = put("/api/posts/{id}", post.getId());

        // 예외 1. 제목 입력 x
        requestAndAssertUpdatePostsExceptions("", post.getContent(), title, "제목을 입력하세요.");

        // 예외 2. 내용 입력 x
        requestAndAssertUpdatePostsExceptions(title, "", content, "내용을 입력하세요.");

        // 예외 3. 제목 100글자 초과
        requestAndAssertUpdatePostsExceptions("t".repeat(101), post.getContent(),
                title, "글 제목은 100글자 이하여야 합니다.");

        // 예외 4. 내용 3000글자 초과
        requestAndAssertUpdatePostsExceptions(post.getTitle(), "c".repeat(3001),
                content, "글 내용은 3000글자 이하여야 합니다.");

        // 예외 5. 게시글 존재 x
        updatePostsWithNonExistentPostId();
    }

    private Posts savePost() {
        Long postId = testDataHelper.savePosts(user);
        return assertOptionalAndGetEntity(postsRepository::findById, postId);
    }

    private void requestAndAssertUpdatePostsExceptions(String title, String content,
                                                       String expectedField, String expectedDefaultMessage) throws Exception {
        PostsUpdateDto requestDto = createPostsUpdateDto(title, content);
        ResultMatcher[] resultMatchers = mockMvcTestHelper.createResultMatchers(expectedField, expectedDefaultMessage);

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private PostsUpdateDto createPostsUpdateDto(String title, String content) {
        return PostsUpdateDto.builder().title(title).content(content).build();
    }

    private void updatePostsWithNonExistentPostId() throws Exception {
        deletePost();

        PostsUpdateDto requestDto = createPostsUpdateDto("updated title", "updated content");
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher("해당 게시글이 없습니다.");

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatcher);
    }

    private void deletePost() {
        postsRepository.delete(post);

        Optional<Posts> result = postsRepository.findById(post.getId());
        assertThat(result).isNotPresent();
    }

    @Test
    @WithMockUser(username = "username1")
    public void deletePostsExceptions() throws Exception {
        // 예외 1. 게시글 존재 x
        deletePostsWithNonExistentPostId();
    }

    private void deletePostsWithNonExistentPostId() throws Exception {
        this.requestBuilder = delete("/api/posts/{id}", 1);
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher("해당 게시글이 없습니다.");

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, null, resultMatcher);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedPostsExceptions() throws Exception {
        this.requestBuilder = delete("/api/posts/selected-delete");

        // 예외 1. 삭제할 항목 선택 x
        deleteSelectedPostsWithEmptyPostIds();

        // 예외 2. 게시글 존재 x
        deleteSelectedPostsWithNonExistentPostIds();
    }

    private void deleteSelectedPostsWithEmptyPostIds() throws Exception {
        List<Long> requestDto = new ArrayList<>();
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher("삭제할 항목을 선택하세요.");

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatcher);
    }

    private void deleteSelectedPostsWithNonExistentPostIds() throws Exception {
        List<Long> requestDto = createPostIds();
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher("해당 게시글이 없습니다.");

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatcher);
    }

    private List<Long> createPostIds() {
        List<Long> requestDto = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long postId = savePost().getId();
            requestDto.add(postId + 1);
        }
        return requestDto;
    }
}
