package com.eskgus.nammunity.web.posts;

import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import org.assertj.core.api.Assertions;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

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

    @BeforeEach
    public void setUp() {
        this.mockMvc = testDB.setUp();

        // 1. user1 회원가입
        testDB.signUp(1L, Role.USER);
        Assertions.assertThat(userRepository.count()).isOne();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    @WithMockUser(username = "username1")
    public void savePosts() throws Exception {
        // 1. user1 회원가입
        userRepository.findById(1L).get();

        // 2. title, content로 PostsSaveDto 생성
        String title = "title1";
        String content = "content1";
        PostsSaveDto requestDto = PostsSaveDto.builder().title(title).content(content).build();

        // 3. "/api/posts"로 postsSaveDto 담아서 post 요청
        MvcResult mvcResult = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 4. 응답 확인
        MvcResultDto mvcResultDto = MvcResultDto.builder()
                .mvcResult(mvcResult).expectedResult(true).title(title).content(content).build();
        assertMvcResult(mvcResultDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void updatePosts() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 3. title, content로 PostsUpdateDto 생성
        String title = "updated title";
        String content = "updated content";
        PostsUpdateDto requestDto = PostsUpdateDto.builder().title(title).content(content).build();

        // 4. "/api/posts/{id}"의 pathVariable=postId로 하고, postsUpdateDto 담아서 put 요청
        MvcResult mvcResult = mockMvc.perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 5. 응답 확인
        MvcResultDto mvcResultDto = MvcResultDto.builder()
                .mvcResult(mvcResult).expectedResult(true).title(title).content(content).build();
        assertMvcResult(mvcResultDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deletePosts() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 3. "/api/posts/{id}"의 pathVariable=postId로 하고, delete 요청
        MvcResult mvcResult = mockMvc.perform(delete("/api/posts/{id}", 1))
                .andExpect(status().isOk())
                .andReturn();

        // 4. 응답 확인
        MvcResultDto mvcResultDto = MvcResultDto.builder()
                .mvcResult(mvcResult).expectedResult(false).build();
        assertMvcResult(mvcResultDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void deleteSelectedPosts() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성 * 2
        Long postId1 = testDB.savePosts(user1);
        Long postId2 = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isEqualTo(2);

        // 3. "/api/posts/selected-delete"로 List<Long> postsId에 postId1/2 담아서 delete 요청
        List<Long> postsId = List.of(postId1, postId2);
        MvcResult mvcResult = mockMvc.perform(delete("/api/posts/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(postsId)))
                .andExpect(status().isOk())
                .andReturn();

        // 4. 응답 확인
        MvcResultDto mvcResultDto = MvcResultDto.builder()
                .mvcResult(mvcResult).expectedResult(false).build();
        assertMvcResult(mvcResultDto);
    }

    @Getter
    private static class MvcResultDto {
        private MvcResult mvcResult;
        private boolean expectedResult;
        private String title;
        private String content;

        @Builder
        public MvcResultDto(MvcResult mvcResult, boolean expectedResult, String title, String content) {
            this.mvcResult = mvcResult;
            this.expectedResult = expectedResult;
            this.title = title;
            this.content = content;
        }
    }

    private void assertMvcResult(MvcResultDto mvcResultDto) throws Exception {
        // 1. 응답으로 "OK" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResultDto.getMvcResult().getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 2. expectedResult가 false면 게시글 삭제한 거 => db에 있는 게시글 수 0인지 확인하고 리턴
        if (!mvcResultDto.isExpectedResult()) {
            Assertions.assertThat(postsRepository.count()).isZero();
            return;
        }

        // 3. expectedResult가 true면 게시글 삭제 말고 저장 또는 수정한 거
        // 3-1. "OK"의 값으로 posts 찾고
        Long id = Long.valueOf((String) map.get("OK"));
        Optional<Posts> result = postsRepository.findById(id);
        Assertions.assertThat(result).isPresent();

        // 3-2. db에 저장됐나 확인
        Posts posts = result.get();
        Assertions.assertThat(posts.getTitle()).isEqualTo(mvcResultDto.getTitle());
        Assertions.assertThat(posts.getContent()).isEqualTo(mvcResultDto.getContent());
    }
}
