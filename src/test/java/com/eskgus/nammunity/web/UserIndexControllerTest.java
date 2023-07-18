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
        String body = this.testRestTemplate.getForObject("/user/sign-up", String.class);

        Assertions.assertThat(body).contains("회원가입");
    }

    @Test
    public void loadSignInUserPage() {
        // 1. "/users/sign-in"으로 get 요청
        String body = testRestTemplate.getForObject("/users/sign-in", String.class);

        // 2. 화면에 "로그인" 있나 확인
        Assertions.assertThat(body).contains("로그인");
    }
}
