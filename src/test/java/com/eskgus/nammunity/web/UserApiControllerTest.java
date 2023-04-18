package com.eskgus.nammunity.web;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.RegistrationDto;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserApiControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @AfterEach
    public void cleanUp() throws Exception {
        userRepository.deleteAll();
    }

    @Test
    public void signUpUser() throws Exception {
        RegistrationDto requestDto = RegistrationDto.builder()
                .username("username123").password("password123").nickname("nick네ME123").build();

        String url = "http://localhost:" + port + "/api/user";
        ResponseEntity<Long> responseEntity = testRestTemplate.postForEntity(url, requestDto, Long.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity.getBody()).isGreaterThan(0L);

        List<User> all = userRepository.findAll();
        boolean result = encoder.matches("password123", all.get(0).getPassword());
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void checkUsername() throws Exception {
        String username = "username123";
        String password = "password123!";
        String nickname = "nick네임123";
        userRepository.save(User.builder().username(username).password(password).nickname(nickname).build());

        // username123으로 회원가입 시도하는 다른 사용자1
        RegistrationDto requestDto1 = RegistrationDto.builder()
                .username(username).password(password).nickname(nickname).build();

        HttpEntity<RegistrationDto> requestEntity1 = new HttpEntity<>(requestDto1);

        String url1 = "http://localhost:" + port + "/api/exists/username/" + username;

        ResponseEntity<Boolean> responseEntity1 = testRestTemplate.exchange(url1, HttpMethod.GET, requestEntity1, Boolean.class);

        Assertions.assertThat(responseEntity1.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity1.getBody()).isTrue();

        // username1234로 회원가입 시도하는 또 다른 사용자2
        RegistrationDto requestDto2 = RegistrationDto.builder()
                .username("username1234").password(password).nickname(nickname).build();
        HttpEntity<RegistrationDto> requestEntity2 = new HttpEntity<>(requestDto2);
        String url2 = "http://localhost:" + port + "/api/exists/username/" + "username1234";
        ResponseEntity<Boolean> responseEntity2 = testRestTemplate.exchange(url2, HttpMethod.GET, requestEntity2, Boolean.class);
        Assertions.assertThat(responseEntity2.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(responseEntity2.getBody()).isFalse();
    }
}
