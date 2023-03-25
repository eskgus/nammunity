package com.eskgus.nammunity.web;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class IndexControllerTest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private PostsRepository postsRepository;

    @Test
    public void loadMainPage() {
        String body = this.testRestTemplate.getForObject("/", String.class);

        Assertions.assertThat(body).contains("나뮤니티: 나현이가 만든 커뮤니티");
    }

    @Test
    public void readAPost() {
        postsRepository.save(Posts.builder().title("title").content("content").author("author").build());

        Posts posts = postsRepository.findAll().get(0);

        String body = this.testRestTemplate.getForObject("/posts/read/" + posts.getId(), String.class);
        Assertions.assertThat(body).contains("value=\"title\"");

        posts = postsRepository.findAll().get(0);
        Assertions.assertThat(posts.getView()).isEqualTo(1);
    }
}
