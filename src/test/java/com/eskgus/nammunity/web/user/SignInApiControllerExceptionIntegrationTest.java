package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.reports.ContentReportSummaryRepository;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.user.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Period;
import java.util.Optional;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.EMAIL;
import static com.eskgus.nammunity.domain.enums.Fields.USERNAME;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SignInApiControllerExceptionIntegrationTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContentReportSummaryRepository reportSummaryRepository;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    private static final String REQUEST_MAPPING = "/api/users/sign-in";

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    @WithAnonymousUser
    public void findUsernameWithEmptyEmail() throws Exception {
        testFindUsernameException(EMPTY_EMAIL, "");
    }

    @Test
    @WithAnonymousUser
    public void findUsernameWithInvalidEmail() throws Exception {
        testFindUsernameException(INVALID_EMAIL, EMAIL.getKey());
    }

    @Test
    @WithAnonymousUser
    public void findUsernameWithNonExistentEmail() throws Exception {
        testFindUsernameException(NON_EXISTENT_USER_EMAIL, EMAIL.getKey() + "@naver.com");
    }

    @Test
    @WithAnonymousUser
    public void findPasswordWithEmptyUsername() throws Exception {
        testFindPasswordException(EMPTY_USERNAME, "");
    }

    @Test
    @WithAnonymousUser
    public void findPasswordWithNonExistentUsername() throws Exception {
        testFindPasswordException(NON_EXISTENT_USER, USERNAME.getKey());
    }

    @Test
    @WithAnonymousUser
    public void findPasswordWithBannedUser() throws Exception {
        // given
        Pair<User, User> users = saveUsers();
        User user = users.getFirst();
        User reporter = users.getSecond();

        saveUserReportSummary(user, reporter);
        saveBannedUser(user);

        // when/then
        testFindPasswordException(BANNED_USER, user.getUsername());
    }

    private Pair<User, User> saveUsers() {
        User user1 = saveUser(1L);
        User user2 = saveUser(2L);

        return Pair.of(user1, user2);
    }

    private User saveUser(Long id) {
        Long userId = testDataHelper.signUp(id, Role.USER);
        return assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    private void saveUserReportSummary(User user, User reporter) {
        Long userReportSummaryId = testDataHelper.saveUserReportSummary(user, reporter);
        assertOptionalAndGetEntity(reportSummaryRepository::findById, userReportSummaryId);
    }

    private void saveBannedUser(User user) {
        Long bannedUserId = testDataHelper.saveBannedUsers(user, Period.ofWeeks(1));
        assertOptionalAndGetEntity(bannedUsersRepository::findById, bannedUserId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    private void testFindUsernameException(ExceptionMessages exceptionMessage, String email) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get(REQUEST_MAPPING + "/username");
        ResultMatcher resultMatcher = createResultMatcher(exceptionMessage);
        mockMvcTestHelper.performAndExpectBadRequestWithParam(requestBuilder, EMAIL, email, resultMatcher);
    }

    private void testFindPasswordException(ExceptionMessages exceptionMessage, String username) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = put(REQUEST_MAPPING + "/password");
        ResultMatcher resultMatcher = createResultMatcher(exceptionMessage);
        mockMvcTestHelper.performAndExpectBadRequestWithParam(requestBuilder, USERNAME, username, resultMatcher);
    }

    private ResultMatcher createResultMatcher(ExceptionMessages exceptionMessage) {
        return mockMvcTestHelper.createResultMatcher(exceptionMessage);
    }
}
