package com.eskgus.nammunity.web.likes;

import com.eskgus.nammunity.domain.likes.LikesRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LikesApiControllerExceptionTest extends LikesApiControllerTest {
    @Autowired
    private LikesRepository likesRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .build();

        signUp();
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void causeExceptionsInSavingLikes() throws Exception {
        // 예외 1. 게시글 존재 x
        MvcResult mvcResult1 = mockMvc.perform(post("/api/likes")
                        .param("postsId", "1"))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("게시글이 없");
        Assertions.assertThat(likesRepository.count()).isZero();

        // 예외 2. 댓글 존재 x
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void causeExceptionsInDeletingLikes() throws Exception {
        // 예외 1. 게시글 존재 x
        MvcResult mvcResult1 = mockMvc.perform(delete("/api/likes")
                        .param("postsId", "1"))
                .andExpect(status().isOk())
                .andReturn();
        Assertions.assertThat(mvcResult1.getResponse().getContentAsString()).contains("게시글이 없");
        Assertions.assertThat(likesRepository.count()).isZero();

        // 예외 2. 댓글 존재 x
    }
}
