package com.eskgus.nammunity.domain.user;

import com.eskgus.nammunity.TestDB;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserRepositoryTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void existsByUser() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        Assertions.assertThat(userRepository.count()).isOne();

        // 2. 존재하는 username으로 호출
        String username = user1.getUsername();
        callAndAssertExistsByUser(username, true);

        // 3. 존재하지 않는 username으로 호출
        callAndAssertExistsByUser(username + 1, false);
    }

    private void callAndAssertExistsByUser(String username, boolean expectedResult) {
        // 1. username으로 existsByUsername() 호출
        boolean result = userRepository.existsByUsername(username);

        // 2. 리턴 값이 expectedValue인지 확인
        Assertions.assertThat(result).isEqualTo(expectedResult);
    }
}
