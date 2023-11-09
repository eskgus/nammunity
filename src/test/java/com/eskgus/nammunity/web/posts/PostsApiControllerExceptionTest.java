package com.eskgus.nammunity.web.posts;

import com.eskgus.nammunity.TestDB;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerExceptionTest {
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
    public void causeExceptionsOnSavingPosts() throws Exception {
        // 1. user1 회원가입
        // 예외 1. 제목/내용 입력 x
        requestAndAssertForExceptionOnSavingPosts("", "", "입력");

        // 예외 2. 제목 100글자 초과, 내용 3000글자 초과 입력
        String title = "t";
        String content = "c";
        requestAndAssertForExceptionOnSavingPosts(title.repeat(101), content.repeat(3001), "00글자 이하");
    }

    @Test
    @WithMockUser(username = "username1")
    public void causeExceptionsOnUpdatingPosts() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(user1);
        Assertions.assertThat(postsRepository.count()).isOne();

        // 예외 1. 제목/내용 입력 x
        UpdatingPostsRequestDto emptyContentRequestDto = UpdatingPostsRequestDto.builder()
                .title("").content("").postId(postId).responseValue("입력").build();
        requestAndAssertForExceptionOnUpdatingPosts(emptyContentRequestDto);

        // 예외 2. 제목 100글자 초과, 내용 3000글자 초과 입력
        String title = "t";
        String content = "c";
        UpdatingPostsRequestDto longContentRequestDto = UpdatingPostsRequestDto.builder()
                .title(title.repeat(101)).content(content.repeat(3001))
                .postId(postId).responseValue("00글자 이하").build();
        requestAndAssertForExceptionOnUpdatingPosts(longContentRequestDto);

        // 예외 3. 게시글 존재 x
        UpdatingPostsRequestDto invalidPostRequestDto = UpdatingPostsRequestDto.builder()
                .title(title).content(content).postId(postId + 1).responseValue("게시글이 없").build();
        requestAndAssertForExceptionOnUpdatingPosts(invalidPostRequestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void causeExceptionsOnDeletingPosts() throws Exception {
        // 1. user1 회원가입
        // 예외 1. 게시글 존재 x
        MvcResult mvcResult = mockMvc.perform(delete("/api/posts/{id}", 1))
                .andExpect(status().isOk())
                .andReturn();
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");
        Assertions.assertThat((String) map.get("error")).contains("게시글이 없");
        Optional<Posts> result = postsRepository.findById(1L);
        Assertions.assertThat(result).isNotPresent();
    }

    @Test
    @WithMockUser(username = "username1")
    public void causeExceptionsOnDeletingSelectedPosts() throws Exception {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user1이 게시글 작성 * 2
        Long postId1 = testDB.savePosts(user1);
        Long postId2 = testDB.savePosts(user1);
        long numOfPosts = postsRepository.count();
        Assertions.assertThat(numOfPosts).isEqualTo(2);

        // 예외 1. 삭제할 항목 선택 x
        List<Long> postsId = new ArrayList<>();
        requestAndAssertForExceptionOnDeletingSelectedPosts(postsId, "삭제할 항목을 선택", numOfPosts);

        // 예외 2. 게시글 존재 x
        postsId.add(postId1);
        postsId.add(postId2 + 1);
        requestAndAssertForExceptionOnDeletingSelectedPosts(postsId, "게시글이 없", numOfPosts);
    }

    @Getter
    private static class UpdatingPostsRequestDto {
        private String title;
        private String content;
        private Long postId;
        private String responseValue;

        @Builder
        public UpdatingPostsRequestDto(String title, String content, Long postId, String responseValue) {
            this.title = title;
            this.content = content;
            this.postId = postId;
            this.responseValue = responseValue;
        }
    }

    private void requestAndAssertForExceptionOnSavingPosts(String title, String content, String responseValue) throws Exception {
        // 1. title, content로 PostsSaveDto 생성
        PostsSaveDto requestDto = PostsSaveDto.builder()
                .title(title).content(content).build();

        // 2. "/api/posts"로 postsSaveDto 담아서 post 요청
        MvcResult mvcResult = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "title", "content" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKeys("title", "content");

        // 4. "title", "content"의 값이 responseValue인지 확인
        Assertions.assertThat((String) map.get("title")).contains(responseValue);
        Assertions.assertThat((String) map.get("content")).contains(responseValue);

        // 5. db에 저장된 거 없는지 확인
        Assertions.assertThat(postsRepository.count()).isZero();
    }

    private void requestAndAssertForExceptionOnUpdatingPosts(UpdatingPostsRequestDto postsRequestDto) throws Exception {
        // 1. title, content로 PostsUpdateDto 생성
        PostsUpdateDto requestDto = PostsUpdateDto.builder()
                .title(postsRequestDto.getTitle()).content(postsRequestDto.getContent()).build();
        Long postId = postsRequestDto.getPostId();

        // 2. "/api/posts/{id}"의 pathVariable=postId로 하고, postsUpdateDto 담아서 put 요청
        MvcResult mvcResult = mockMvc.perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 응답으로 "error" 아니면 "title", "content" 왔는지 확인
        // 4. "error"나 "title", "content"의 값이 responseValue인지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        String responseValue = postsRequestDto.getResponseValue();
        if (responseValue.contains("게시글이 없")) {
            Assertions.assertThat(map).containsKey("error");
            Assertions.assertThat((String) map.get("error")).contains(responseValue);
        } else {
            Assertions.assertThat(map).containsKeys("title", "content");
            Assertions.assertThat((String) map.get("title")).contains(responseValue);
            Assertions.assertThat((String) map.get("content")).contains(responseValue);
        }

        // 5. postId로 posts 찾고,
        if (postId.equals(2L)) {
            postId -= 1;
        }
        Optional<Posts> result = postsRepository.findById(postId);
        Assertions.assertThat(result).isPresent();

        // 6. title="title", content="content"인지 확인
        Posts post = result.get();
        Assertions.assertThat(post.getTitle()).isEqualTo("title");
        Assertions.assertThat(post.getContent()).isEqualTo("content");
    }

    private void requestAndAssertForExceptionOnDeletingSelectedPosts(List<Long> postsId, String responseValue,
                                                                     long numOfPosts) throws Exception {
        // 1. "/api/posts/selected-delete"로 postsId 담아서 delete 요청
        MvcResult mvcResult = mockMvc.perform(delete("/api/posts/selected-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(postsId)))
                .andExpect(status().isOk())
                .andReturn();

        // 2. 응답으로 "error" 왔는지 확인
        Map<String, Object> map = testDB.parseResponseJSON(mvcResult.getResponse().getContentAsString());
        Assertions.assertThat(map).containsKey("error");

        // 3. "error"의 값이 responseValue인지 확인
        Assertions.assertThat((String) map.get("error")).contains(responseValue);

        // 4. db에 있는 게시글 수가 numOfPosts인지 확인
        Assertions.assertThat(postsRepository.count()).isEqualTo(numOfPosts);
    }
}
