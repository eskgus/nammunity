package com.eskgus.nammunity.web;

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
public class UserIndexControllerTest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void loadSignUpPage() {
        String body = this.testRestTemplate.getForObject("/user/signUp", String.class);

        Assertions.assertThat(body).contains("회원가입");
    }
}
