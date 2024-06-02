package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.converter.UserConverterForTest;
import com.eskgus.nammunity.helper.PaginationTestHelper;
import com.eskgus.nammunity.helper.Range;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.function.Function;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private final int page = 1;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDataHelper.signUp(1L, Role.USER);
        assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDataHelper.signUp(2L, Role.ADMIN);
        assertOptionalAndGetEntity(userRepository::findById, user2Id);
    }

    private void assertOptionalAndGetEntity(Function<Long, Optional<User>> finder, Long contentId) {
        testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void searchByNickname() {
        saveUsersWithNickname();

        // 1. 검색 제외 단어 x
        callAndAssertSearchByNickname("nick 사");

        // 2. 검색 제외 단어 o
        callAndAssertSearchByNickname("nick 사 -name");
    }

    private void saveUsersWithNickname() {
        long numberOfUsers = 20;
        long half = numberOfUsers / 2;

        Range firstRange = Range.builder().startIndex(userRepository.count() + 1).endIndex(half).nickname("nickname").build();
        Range secondRange = Range.builder().startIndex(half + 1).endIndex(numberOfUsers).nickname("사용자").build();

        saveUsersInRange(firstRange);
        saveUsersInRange(secondRange);
    }

    private void saveUsersInRange(Range range) {
        for (long i = range.getStartIndex(); i <= range.getEndIndex(); i++) {
            Long userId = testDataHelper.signUp(range.getNickname() + i, i, Role.USER);
            assertOptionalAndGetEntity(userRepository::findById, userId);
        }
    }

    private void callAndAssertSearchByNickname(String keywords) {
        int size = 3;

        Page<UsersListDto> actualContents = userService.searchByNickname(keywords, page, size);
        Page<UsersListDto> expectedContents = createExpectedPage(keywords, size);

        assertContents(actualContents, expectedContents);
    }

    private Page<UsersListDto> createExpectedPage(String keywords, int size) {
        Pageable pageable = createPageable(page, size);
        return userRepository.searchByNickname(keywords, pageable);
    }

    private void assertContents(Page<UsersListDto> actualContents, Page<UsersListDto> expectedContents) {
        PaginationTestHelper<UsersListDto, User> paginationHelper
                = new PaginationTestHelper<>(actualContents, expectedContents, new UserConverterForTest());
        paginationHelper.assertContents();
    }
}
