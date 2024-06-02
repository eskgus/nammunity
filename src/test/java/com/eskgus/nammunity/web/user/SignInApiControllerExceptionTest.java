package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.domain.reports.ContentReportSummaryRepository;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.user.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Period;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SignInApiControllerExceptionTest {
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

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void findUsernameExceptions() throws Exception {
        // 예외 1. 이메일 입력 x
        requestAndAssertFindUsernameExceptions("", "이메일을 입력하세요.");

        // 예외 2. 이메일 형식 x
        requestAndAssertFindUsernameExceptions("email", "이메일 형식이 맞지 않습니다.");

        // 예외 3. 가입되지 x 이메일
        requestAndAssertFindUsernameExceptions("email@naver.com", "가입되지 않은 이메일입니다.");
    }

    private void requestAndAssertFindUsernameExceptions(String email, String expectedContent) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/api/users/sign-in/username");
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher(expectedContent);

        mockMvcTestHelper.requestAndAssertStatusIsBadRequestWithParam(
                requestBuilder, "email", email, resultMatcher);
    }

    @Test
    public void findPasswordExceptions() throws Exception {
        // 예외 1. username 입력 x
        requestAndAssertFindPasswordExceptions("", "ID를 입력하세요.");

        // 예외 2. 존재하지 x username
        requestAndAssertFindPasswordExceptions("username", "존재하지 않는 ID입니다.");

        // 예외 3. 활동 정지된 사용자
        findPasswordWithBannedUser();
    }

    private void requestAndAssertFindPasswordExceptions(String username, String expectedContent) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = put("/api/users/sign-in/password");
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher(expectedContent);

        mockMvcTestHelper.requestAndAssertStatusIsBadRequestWithParam(
                requestBuilder, "username", username, resultMatcher);
    }

    private void findPasswordWithBannedUser() throws Exception {
        User bannedUser = banUser();

        requestAndAssertFindPasswordExceptions(bannedUser.getUsername(),
                "활동 정지된 계정입니다. 자세한 내용은 메일을 확인하세요.");
    }

    private User banUser() {
        User user = saveUser(1L);
        User reporter = saveUser(2L);

        saveUserReportSummary(user, reporter);
        saveBannedUser(user);

        return user;
    }

    private User saveUser(Long id) {
        Long userId = testDataHelper.signUp(id, Role.USER);
        return assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }

    private void saveUserReportSummary(User user, User reporter) {
        Long userReportSummaryId = testDataHelper.saveUserReportSummary(user, reporter);
        assertOptionalAndGetEntity(reportSummaryRepository::findById, userReportSummaryId);
    }

    private void saveBannedUser(User user) {
        Long bannedUserId = testDataHelper.saveBannedUsers(user, Period.ofWeeks(1));
        assertOptionalAndGetEntity(bannedUsersRepository::findById, bannedUserId);
    }
}
