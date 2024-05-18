package com.eskgus.nammunity.web.posts;

import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.*;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {
    @Autowired
    private TestDB testDB;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        this.mockMvc = testDB.setUp();

        Long userId = testDB.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);
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
    public void savePosts() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/api/posts");
        PostsSaveDto requestDto = createPostsSaveDto();

        requestAndAssert(requestBuilder, requestDto);
    }

    private PostsSaveDto createPostsSaveDto() {
        String title = "title";
        String content = "content";
        return PostsSaveDto.builder().title(title).content(content).build();
    }

    private <T> void requestAndAssert(MockHttpServletRequestBuilder requestBuilder, T requestDto) throws Exception {
        mockMvc.perform(requestBuilder
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePosts() throws Exception {
        Long postId = savePost();
        MockHttpServletRequestBuilder requestBuilder = put("/api/posts/{id}", postId);
        PostsUpdateDto requestDto = createPostsUpdateDto();

        requestAndAssert(requestBuilder, requestDto);
    }

    private Long savePost() {
        Long postId = testDB.savePosts(user);
        assertOptionalAndGetEntity(postsRepository::findById, postId);
        return postId;
    }

    private PostsUpdateDto createPostsUpdateDto() {
        String title = "updated title";
        String content = "updated content";
        return PostsUpdateDto.builder().title(title).content(content).build();
    }

    @Test
    @WithMockUser(username = "username1")
    public void deletePosts() throws Exception {
        Long postId = savePost();

        MockHttpServletRequestBuilder requestBuilder = delete("/api/posts/{id}", postId);

        requestAndAssert(requestBuilder, null);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedPosts() throws Exception {
        List<Long> requestDto = createPostIds();
        MockHttpServletRequestBuilder requestBuilder = delete("/api/posts/selected-delete");

        requestAndAssert(requestBuilder, requestDto);
    }

    private List<Long> createPostIds() {
        List<Long> requestDto = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Long postId = savePost();
            requestDto.add(postId);
        }
        return requestDto;
    }
}
