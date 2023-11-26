package com.eskgus.nammunity.domain.user;

import com.eskgus.nammunity.util.SearchUtil;
import com.eskgus.nammunity.TestDB;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static com.eskgus.nammunity.util.SearchUtil.callAndAssertSearchByField;
import static com.eskgus.nammunity.util.SearchUtil.getExpectedIdList;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserRepositoryTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        testDB.signUp(1L, Role.USER);
        Assertions.assertThat(userRepository.count()).isOne();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void existsByUser() {
        // 1. user1 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. 존재하는 username으로 호출
        String username = user1.getUsername();
        callAndAssertExistsByUser(username, true);

        // 3. 존재하지 않는 username으로 호출
        callAndAssertExistsByUser(username + 1, false);
    }

    @Test
    public void searchByNickname() {
        // 1. user1 회원가입 + user2 회원가입 + user3 회원가입
        testDB.signUp(2L, Role.USER);
        testDB.signUp("닉네임", 3L, Role.USER);
        Assertions.assertThat(userRepository.count()).isEqualTo(3);

        // 2. 예상 결과(List<User>) 생성
        // 2-1. 전체 사용자, 검색어, 검색 제외 단어 준비
        List<User> users = userRepository.findAll();
        String[] includeKeywords = { "nick", "네임" };
        String[] excludeKeywords = { "name" };

        // 2-2. 검색 제외 단어 없이 호출할 때 예상 결과
        SearchUtil.SearchDto<User> searchDto1 = SearchUtil.SearchDto.<User>builder()
                .contents(users).fieldExtractor(User::getNickname).idExtractor(User::getId)
                .includeKeywords(includeKeywords).build();
        List<Long> expectedIdList1 = getExpectedIdList(searchDto1);

        // 2-3. 검색 제외 단어 포함해서 호출할 때 예상 결과
        SearchUtil.SearchDto<User> searchDto2 = SearchUtil.SearchDto.<User>builder()
                .contents(users).fieldExtractor(User::getNickname).idExtractor(User::getId)
                .includeKeywords(includeKeywords).excludeKeywords(excludeKeywords).build();
        List<Long> expectedIdList2 = getExpectedIdList(searchDto2);

        // 3. searchByNickname() 호출
        // 3-1. 검색 제외 단어 x
        callAndAssertSearchByField("nick 네임", userRepository::searchByNickname,
                searchDto1.getIdExtractor(), expectedIdList1);

        // 3-2. 검색 제외 단어 o
        callAndAssertSearchByField("nick 네임 -name", userRepository::searchByNickname,
                searchDto2.getIdExtractor(), expectedIdList2);
    }

    private void callAndAssertExistsByUser(String username, boolean expectedResult) {
        // 1. username으로 existsByUsername() 호출
        boolean result = userRepository.existsByUsername(username);

        // 2. 리턴 값이 expectedValue인지 확인
        Assertions.assertThat(result).isEqualTo(expectedResult);
    }
}
