package com.eskgus.nammunity.web;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.web.dto.posts.PostsSaveDto;
import com.eskgus.nammunity.web.dto.posts.PostsUpdateDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private PostsRepository postsRepository;

    @AfterEach
    public void cleanUp() throws Exception{
        postsRepository.deleteAll();
    }

    @Test
    public void savePosts() throws Exception {
        String title = "test title";
        String content = "test content";
        PostsSaveDto requestDto = PostsSaveDto.builder()
                .title(title).content(content).author("test author").build();
        String url = "http://localhost:" + port + "/api/posts";

        ResponseEntity<Long> responseEntity = testRestTemplate.postForEntity(url, requestDto, Long.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isGreaterThan(0L);

        List<Posts> all = postsRepository.findAll();

        Assertions.assertThat(all.get(0).getTitle()).isEqualTo(title);
        Assertions.assertThat(all.get(0).getContent()).isEqualTo(content);
    }

    @Test
    public void updatePosts() throws Exception {
        Posts posts = postsRepository.save(Posts.builder().title("title").content("content").author("author").build());

        Long id = posts.getId();
        String modifiedTitle = "modified Title";
        String modifiedContent = "modified Content";

        PostsUpdateDto requestDto = PostsUpdateDto.builder()
                .title(modifiedTitle).content(modifiedContent).build();
        HttpEntity<PostsUpdateDto> requestEntity = new HttpEntity<>(requestDto);

        String url = "http://localhost:" + port + "/api/posts/" + id;

        ResponseEntity<Long> responseEntity = testRestTemplate.exchange(url, HttpMethod.PUT, requestEntity, Long.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isGreaterThan(0L);

        posts = postsRepository.findAll().get(0);
        Assertions.assertThat(posts.getTitle()).isEqualTo(modifiedTitle);
        Assertions.assertThat(posts.getContent()).isEqualTo(modifiedContent);
    }

    @Test
    public void deletePosts() throws Exception {
        Posts posts = postsRepository.save(Posts.builder().title("title").content("content").author("author").build());
        Long id = posts.getId();

        String url = "http://localhost:" + port + "/api/posts/" + id;

        ResponseEntity<Long> responseEntity = testRestTemplate
                .exchange(url, HttpMethod.DELETE, new HttpEntity<>(posts), Long.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
