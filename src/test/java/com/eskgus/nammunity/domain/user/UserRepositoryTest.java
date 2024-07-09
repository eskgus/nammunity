package com.eskgus.nammunity.domain.user;

import com.eskgus.nammunity.config.TestConfig;
import com.eskgus.nammunity.converter.UserConverterForTest;
import com.eskgus.nammunity.helper.PaginationTestHelper;
import com.eskgus.nammunity.helper.Range;
import com.eskgus.nammunity.helper.SearchTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;
import static com.eskgus.nammunity.util.PaginationTestUtil.createPageWithContent;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({ TestDataHelper.class, TestConfig.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDataHelper.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, user1Id);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
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
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void searchByNickname() {
        saveUsers();

        // 1. 검색 제외 단어 x
        callAndAssertSearch("nick 사");

        // 2. 검색 제외 단어 o
        callAndAssertSearch("nick 사 -name");
    }

    private void saveUsers() {
        long numberOfUsers = 10;
        long half = numberOfUsers / 2;

        Range firstRange = Range.builder().startIndex(2).endIndex(half).nickname("nickname").build();
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

    private final UserConverterForTest entityConverter = new UserConverterForTest();
    private void callAndAssertSearch(String keywords) {
        int page = 1;
        int size = 2;

        Pageable pageable = createPageable(page, size);

        Page<UsersListDto> actualContents = userRepository.searchByNickname(keywords, pageable);
        Page<UsersListDto> expectedContents = createExpectedContents(keywords, pageable, User::getNickname);

        assertContents(actualContents, expectedContents);
    }

    private Page<UsersListDto> createExpectedContents(String keywords, Pageable pageable,
                                                      Function<User, String>... fieldExtractors) {
        SearchTestHelper<User> searchHelper = SearchTestHelper.<User>builder()
                .totalContents(userRepository.findAll()).keywords(keywords)
                .fieldExtractors(fieldExtractors).build();
        Stream<User> filteredUsersStream = searchHelper.getKeywordsFilter();

        return createPageWithContent(filteredUsersStream, entityConverter, pageable);
    }

    private void assertContents(Page<UsersListDto> actualContents, Page<UsersListDto> expectedContents) {
        PaginationTestHelper<UsersListDto, User> paginationHelper
                = new PaginationTestHelper<>(actualContents, expectedContents, entityConverter);
        paginationHelper.assertContents();
    }
}
