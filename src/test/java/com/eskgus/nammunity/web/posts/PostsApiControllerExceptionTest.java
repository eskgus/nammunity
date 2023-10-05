package com.eskgus.nammunity.web.posts;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerExceptionTest extends PostsApiControllerTest {
    @Autowired
    private PostsRepository postsRepository;

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
    public void causeExceptionsInSavingPosts() throws Exception {
        // 예외 1. 제목/내용 입력 x
        PostsSaveDto requestDto1 = PostsSaveDto.builder().title("").content("").build();
        MvcResult mvcResult1 = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto1)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKeys("title", "content");
        Assertions.assertThat((String) map.get("title")).contains("입력");
        Assertions.assertThat((String) map.get("content")).contains("입력");

        // 예외 2. 제목 100글자 초과, 내용 3000글자 초과 입력
        String title = "t";
        String content = "c";
        PostsSaveDto requestDto2 = PostsSaveDto.builder()
                .title(title.repeat(101))
                .content(content.repeat(3001)).build();
        MvcResult mvcResult2 = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto2)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult2.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKeys("title", "content");
        Assertions.assertThat((String) map.get("title")).contains("100글자 이하");
        Assertions.assertThat((String) map.get("content")).contains("3000글자 이하");

        Assertions.assertThat(postsRepository.count()).isZero();
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void causeExceptionsInUpdatingPosts() throws Exception {
        savePosts();

        // 예외 1. 제목/내용 입력 x
        PostsUpdateDto requestDto1 = PostsUpdateDto.builder().title("").content("").build();
        MvcResult mvcResult1 = mockMvc.perform(put("/api/posts/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto1)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKeys("title", "content");
        Assertions.assertThat((String) map.get("title")).contains("입력");
        Assertions.assertThat((String) map.get("content")).contains("입력");

        // 예외 2. 제목 100글자 초과, 내용 3000글자 초과 입력
        String title = "t";
        String content = "c";
        PostsUpdateDto requestDto2 = PostsUpdateDto.builder()
                .title(title.repeat(101))
                .content(content.repeat(3001)).build();
        MvcResult mvcResult2 = mockMvc.perform(put("/api/posts/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto2)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult2.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKeys("title", "content");
        Assertions.assertThat((String) map.get("title")).contains("100글자 이하");
        Assertions.assertThat((String) map.get("content")).contains("3000글자 이하");

        // 예외 3. 게시글 존재 x
        PostsUpdateDto requestDto3 = PostsUpdateDto.builder().title(title).content(content).build();
        MvcResult mvcResult3 = mockMvc.perform(put("/api/posts/{id}", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto3)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult3.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("게시글이 없");
        Assertions.assertThat(postsRepository.findById(2L)).isNotPresent();

        Posts posts = postsRepository.findById(1L).get();
        Assertions.assertThat(posts.getTitle()).isEqualTo("title1");
        Assertions.assertThat(posts.getContent()).isEqualTo("content1");
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void causeExceptionsInDeletingPosts() throws Exception {
        // 예외 1. 게시글 존재 x
        MvcResult mvcResult = mockMvc.perform(delete("/api/posts/{id}", 1))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("게시글이 없");
        Optional<Posts> result = postsRepository.findById(1L);
        Assertions.assertThat(result).isNotPresent();
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void causeExceptionsInDeletingSelectedPosts() throws Exception {
        savePosts();
        savePosts();

        List<Long> postsId = new ArrayList<>();

        // 예외 1. 삭제할 항목 선택 x
        MvcResult mvcResult1 = mockMvc.perform(delete("/api/posts/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(postsId)))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("삭제할 항목을 선택");

        // 예외 2. 게시글 존재 x
        postsId.add(1L);
        postsId.add(3L);
        MvcResult mvcResult2 = mockMvc.perform(delete("/api/posts/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(postsId)))
                .andExpect(status().isOk())
                .andReturn();
        map = parseResponseJSON(mvcResult2.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("게시글이 없");

        Assertions.assertThat(postsRepository.count()).isGreaterThan(1L);
    }
}
