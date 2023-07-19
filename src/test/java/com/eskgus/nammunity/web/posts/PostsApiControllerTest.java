package com.eskgus.nammunity.web.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.user.UserApiControllerTest;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.*;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Log4j2
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest extends UserApiControllerTest {
    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .build();

        signUp();
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void save() throws Exception {
        // 1. 회원가입 (+ 로그인) 후
        User user = userRepository.findById(1L).get();

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

        // 4. 응답으로 "OK" 왔나 확인
        Map<String, Object> map = parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 5. 응답으로 온 post id 이용해서 게시글이 db에 저장됐나 확인;
        Long id = Long.valueOf((String) map.get("OK"));
        Optional<Posts> result = postsRepository.findById(id);
        Assertions.assertThat(result).isPresent();
        Posts posts = result.get();
        Assertions.assertThat(posts.getTitle()).isEqualTo(title);
        Assertions.assertThat(posts.getContent()).isEqualTo(content);
        Assertions.assertThat(posts.getUser().getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void update() throws Exception {
        // 1. 회원가입 (+ 로그인) + 게시글 저장 후
        save();

        // 2. title, content로 PostsUpdateDto 생성
        String title = "title1 update";
        String content = "content1 update";
        PostsUpdateDto requestDto = PostsUpdateDto.builder().title(title).content(content).build();

        // 3. "/api/posts/1"로 postsUpdateDto 담아서 put 요청
        MvcResult mvcResult = mockMvc.perform(put("/api/posts/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 4. 응답으로 "OK"가 왔나 확인
        Map<String, Object> map = parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 5. db에 저장됐나 확인
        Long id = Long.valueOf((String) map.get("OK"));
        Optional<Posts> result = postsRepository.findById(id);
        Assertions.assertThat(result).isPresent();
        Posts posts = result.get();
        Assertions.assertThat(posts.getTitle()).isEqualTo(title);
        Assertions.assertThat(posts.getContent()).isEqualTo(content);
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void deletePosts() throws Exception {
        // 1. 회원가입 (+ 로그인) + 게시글 저장 후
        save();

        // 2. "/api/posts/1"로 delete 요청
        MvcResult mvcResult = mockMvc.perform(delete("/api/posts/{id}", 1))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔나 확인
        Map<String, Object> map = parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 4. "OK"의 value에 "삭제" 있는지 확인
        Assertions.assertThat((String) map.get("OK")).contains("삭제");

        // 5. db에 1번 게시글이 존재 안 하는지 확인
        Optional<Posts> result = postsRepository.findById(1L);
        Assertions.assertThat(result).isNotPresent();
    }
}
