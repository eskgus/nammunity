package com.eskgus.nammunity.domain.user;

import com.eskgus.nammunity.helper.SearchHelperForTest;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserRepositoryTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        Long userId = testDB.signUp(1L, Role.USER);
        assertThat(userRepository.count()).isEqualTo(userId);

        this.user = userRepository.findById(userId).get();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void existsByUsername() {
        // 1. 존재하는 username으로 호출
        String username = user.getUsername();
        callAndAssertExistsByUser(username, true);

        // 2. 존재하지 않는 username으로 호출
        callAndAssertExistsByUser(username + 1, false);
    }

    private void callAndAssertExistsByUser(String username, boolean expectedResult) {
        boolean result = userRepository.existsByUsername(username);
        Assertions.assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void searchByNickname() {
        signUpUsers();

        // 1. 검색 제외 단어 x
        callAndAssertSearchUsers("nick 네임", userRepository::searchByNickname, User::getNickname);

        // 2. 검색 제외 단어 o
        callAndAssertSearchUsers("nick 네임 -name", userRepository::searchByNickname, User::getNickname);
    }

    private void signUpUsers() {
        Long userId = testDB.signUp(user.getId() + 1, Role.USER);
        Long numberOfUsers = userId + 3;
        for (long i = userId; i < numberOfUsers; i++) {
            testDB.signUp("닉네임" + i, i + 1, Role.USER);
        }
        assertThat(userRepository.count()).isEqualTo(numberOfUsers);
    }

    private void callAndAssertSearchUsers(String keywords,
                                          Function<String, List<UsersListDto>> searcher,
                                          Function<User, String>... fieldExtractors) {
        SearchHelperForTest<UsersListDto, User> searchHelper = createSearchHelper(keywords, searcher, fieldExtractors);
        searchHelper.callAndAssertSearchByField();
    }

    private SearchHelperForTest<UsersListDto, User> createSearchHelper(String keywords,
                                                                       Function<String, List<UsersListDto>> searcher,
                                                                       Function<User, String>[] fieldExtractors) {
        return SearchHelperForTest.<UsersListDto, User>builder()
                .keywords(keywords).searcher(searcher)
                .totalContents(userRepository.findAll(Sort.by(Sort.Order.desc("id"))))
                .fieldExtractors(fieldExtractors).build();
    }
}
