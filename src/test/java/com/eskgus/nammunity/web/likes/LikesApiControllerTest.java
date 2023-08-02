package com.eskgus.nammunity.web.likes;

import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.likes.LikesRepository;
import com.eskgus.nammunity.web.comments.CommentsApiControllerTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LikesApiControllerTest extends CommentsApiControllerTest {
    @Autowired
    private LikesRepository likesRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void saveLikes() throws Exception {
        // 일반 1. 게시글 좋아요
        // 1. 회원가입 + 게시글 작성 후
        // 2. "/api/likes"로 postsId 담아서 post 요청
        MvcResult mvcResult1 = mockMvc.perform(post("/api/likes")
                        .param("postsId", "1"))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔는지 확인
        Map<String, Object> map = parseResponseJSON(mvcResult1.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("OK");

        // 4. db에 저장됐나 확인
        Long id1 = Long.valueOf((String) map.get("OK"));
        Optional<Likes> result1 = likesRepository.findById(id1);
        Assertions.assertThat(result1).isPresent();
        Likes likes = result1.get();
        Assertions.assertThat(likes.getUser().getId()).isOne();
        Assertions.assertThat(likes.getPosts().getId()).isOne();

        // 일반 2. 댓글 좋아요
    }

    @Test
    @WithMockUser(username = "username111", password = "password111")
    public void deleteLikes() throws Exception {
        // 일반 1. 게시글 좋아요 취소
        // 1. 회원가입 + 게시글 작성 + 게시글 좋아요 후
        saveLikes();

        // 2. "/api/likes"로 postsId 담아서 delete 요청
        MvcResult mvcResult1 = mockMvc.perform(delete("/api/likes")
                        .param("postsId", "1"))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "OK" 왔는지 확인
        Assertions.assertThat(mvcResult1.getResponse().getContentAsString()).isEqualTo("OK");

        // 4. db에서 likes 지워졌나 확인
        Optional<Likes> result1 = likesRepository.findById(1L);
        Assertions.assertThat(result1).isNotPresent();

        // 일반 2. 댓글 좋아요 취소
    }
}
