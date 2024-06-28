package com.eskgus.nammunity.web.posts;

import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.*;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.Fields.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerIntegrationTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    private User user;

    private static final String REQUEST_MAPPING = "/api/posts";

    @BeforeEach
    public void setUp() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    @WithMockUser(username = "username1")
    public void savePosts() throws Exception {
        // given
        PostsSaveDto requestDto = PostsSaveDto.builder().title(TITLE.getKey()).content(CONTENT.getKey()).build();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING);
        performAndExpectOk(requestBuilder, requestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePosts() throws Exception {
        // given
        Long postId = savePost();

        String prefix = "updated ";
        PostsUpdateDto requestDto = PostsUpdateDto.builder()
                .title(prefix + TITLE.getKey()).content(prefix + CONTENT.getKey()).build();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/{id}", postId);
        performAndExpectOk(requestBuilder, requestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deletePosts() throws Exception {
        // given
        Long postId = savePost();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/{id}", postId);
        performAndExpectOk(requestBuilder, null);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedPosts() throws Exception {
        // given
        List<Long> requestDto = createPostIds();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/selected-delete");
        performAndExpectOk(requestBuilder, requestDto);
    }

    private List<Long> createPostIds() {
        List<Long> requestDto = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Long postId = savePost();
            requestDto.add(postId);
        }

        return requestDto;
    }

    private Long savePost() {
        Long postId = testDataHelper.savePosts(user);
        assertOptionalAndGetEntity(postsRepository::findById, postId);

        return postId;
    }

    private <Dto> void performAndExpectOk(MockHttpServletRequestBuilder requestBuilder, Dto requestDto) throws Exception {
        mockMvcTestHelper.performAndExpectOk(requestBuilder, requestDto);
    }

    private <Entity> Entity assertOptionalAndGetEntity(Function<Long, Optional<Entity>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }
}
