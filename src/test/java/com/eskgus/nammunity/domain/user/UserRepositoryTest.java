package com.eskgus.nammunity.domain.user;

import com.eskgus.nammunity.config.TestConfig;
import com.eskgus.nammunity.converter.UserConverterForTest;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.helper.PaginationTestHelper;
import com.eskgus.nammunity.helper.Range;
import com.eskgus.nammunity.helper.SearchTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.util.PaginationRepoUtil;
import com.eskgus.nammunity.util.PaginationTestUtil;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private static final Fields USERNAME = Fields.USERNAME;
    private static final Fields NICKNAME = Fields.NICKNAME;
    private static final Fields EMAIL = Fields.EMAIL;

    @BeforeEach
    public void setUp() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void existsUsersByUsernameWithoutUser() {
        testExistsUsersByField(USERNAME, false);
    }

    @Test
    public void existsUsersByUsernameWithUser() {
        testExistsUsersByField(USERNAME, true);
    }

    @Test
    public void existsUsersByNicknameWithoutUser() {
        testExistsUsersByField(NICKNAME, false);
    }

    @Test
    public void existsUserByNicknameWithUser() {
        testExistsUsersByField(NICKNAME, true);
    }

    @Test
    public void existsUsersByEmailWithoutUser() {
        testExistsUsersByField(EMAIL, false);
    }

    @Test
    public void existsUsersByEmailWithUser() {
        testExistsUsersByField(EMAIL, true);
    }

    @Test
    public void findUsersByEmailWithoutUser() {
        testFindUsersByField(EMAIL, false);
    }

    @Test
    public void findUsersByEmailWithUser() {
        testFindUsersByField(EMAIL, true);
    }

    @Test
    public void findUsersByUsernameWithoutUser() {
        testFindUsersByField(USERNAME, false);
    }

    @Test
    public void findUsersByUsernameWithUser() {
        testFindUsersByField(USERNAME, true);
    }

    @Test
    public void searchUsersByNicknameWithoutExcludeKeywords() {
        testSearchUsersByNickname("nick 네임");
    }

    @Test
    public void searchUsersByNicknameWithExcludeKeywords() {
        testSearchUsersByNickname("nick 네임 -닉");
    }

    private void testExistsUsersByField(Fields field, boolean exists) {
        // given
        String value = getValue(field, exists);

        Function<String, Boolean> checker = getChecker(field);

        // when
        boolean result = checker.apply(value);

        // then
        assertEquals(exists, result);
    }

    private void testFindUsersByField(Fields field, boolean present) {
        // given
        String value = getValue(field, present);

        Function<String, Optional<User>> finder = getFinder(field);

        Optional<User> user = present ? Optional.of(this.user) : Optional.empty();

        // when
        Optional<User> result = finder.apply(value);

        // then
        assertEquals(user, result);
    }

    private void testSearchUsersByNickname(String keywords) {
        // given
        saveUsersWithNickname();

        Pageable pageable = PaginationRepoUtil.createPageable(1, 3);

        UserConverterForTest userConverter = new UserConverterForTest();

        SearchTestHelper<User> searchHelper = createSearchHelper(keywords, User::getNickname);
        Page<UsersListDto> usersPage = createUsersPage(searchHelper, userConverter, pageable);

        // when
        Page<UsersListDto> result = userRepository.searchByNickname(keywords, pageable);

        // then
        assertUsersPage(result, usersPage, userConverter);
    }

    private String getValue(Fields field, boolean exists) {
        String value = switch (field) {
            case USERNAME -> user.getUsername();
            case NICKNAME -> user.getNickname();
            default -> user.getEmail();
        };

        return exists ? value : value + user.getId();
    }

    private Function<String, Boolean> getChecker(Fields field) {
        return switch (field) {
            case USERNAME -> userRepository::existsByUsername;
            case NICKNAME -> userRepository::existsByNickname;
            default -> userRepository::existsByEmail;
        };
    }

    private Function<String, Optional<User>> getFinder(Fields field) {
        if (USERNAME.equals(field)) {
            return userRepository::findByUsername;
        } else {
            return userRepository::findByEmail;
        }
    }

    private void saveUsersWithNickname() {
        long numberOfUsers = 10;
        long half = numberOfUsers / 2;

        Range firstRange = Range.builder().startIndex(1 + user.getId()).endIndex(half).nickname("nickname").build();
        Range secondRange = Range.builder().startIndex(half + 1).endIndex(numberOfUsers).nickname("닉네임").build();

        saveUsersInRange(firstRange);
        saveUsersInRange(secondRange);
    }

    private void saveUsersInRange(Range range) {
        for (long i = range.getStartIndex(); i <= range.getEndIndex(); i++) {
            Long userId = testDataHelper.signUp(range.getNickname() + i, i, Role.USER);
            assertOptionalAndGetEntity(userRepository::findById, userId);
        }
    }

    private SearchTestHelper<User> createSearchHelper(String keywords, Function<User, String>... fieldExtractors) {
        return SearchTestHelper.<User>builder()
                .totalContents(userRepository.findAll()).keywords(keywords).fieldExtractors(fieldExtractors).build();
    }

    private Page<UsersListDto> createUsersPage(SearchTestHelper<User> searchHelper, UserConverterForTest userConverter,
                                               Pageable pageable) {
        Stream<User> filtereUsersStream = searchHelper.getKeywordsFilter();

        return PaginationTestUtil.createPageWithContent(filtereUsersStream, userConverter, pageable);
    }

    private void assertUsersPage(Page<UsersListDto> result, Page<UsersListDto> usersPage,
                                 UserConverterForTest userConverter) {
        PaginationTestHelper<UsersListDto, User> paginationHelper
                = new PaginationTestHelper<>(result, usersPage, userConverter);
        paginationHelper.assertContents();
    }

    private <Entity> Entity assertOptionalAndGetEntity(Function<Long, Optional<Entity>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }
}
