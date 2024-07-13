package com.eskgus.nammunity.web.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.reports.ReasonsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDeleteDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportsSaveDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;
import java.util.List;

import static com.eskgus.nammunity.domain.enums.ContentType.*;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportsApiControllerExceptionIntegrationTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReasonsRepository reasonsRepository;

    private static final Long ID = 1L;

    private static final String REQUEST_MAPPING = "/api/reports";

    @BeforeEach
    public void setUp() {
        Long user1Id = testDataHelper.signUp(1L, Role.USER);
        assertOptionalAndGetEntity(user1Id);

        Long user2Id = testDataHelper.signUp(2L, Role.ADMIN);
        assertOptionalAndGetEntity(user2Id);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    @WithAnonymousUser
    public void saveContentReportsWithAnonymousUser() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(ID, POSTS);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/content");
        performAndExpectNotBadRequest(requestBuilder, requestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveContentReportsWithEmptyReasonId() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(null, POSTS);

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(REASONS_ID, null, EMPTY_REASON);
        testSaveContentReportsThrowsMethodArgumentNotValidException(requestDto, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveContentReportsWithInvalidOtherReason() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(ID, POSTS);
        requestDto.setOtherReasons("10 letters".repeat(50) + "!");

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(
                OTHER_REASONS, requestDto.getOtherReasons(), INVALID_OTHER_REASON);
        testSaveContentReportsThrowsMethodArgumentNotValidException(requestDto, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username3")
    public void saveContentReportsWithNonExistentUsername() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(ID, POSTS);

        // when/then
        testSaveContentReportsThrowsIllegalArgumentException(requestDto, USERNAME_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveContentReportsWithNonExistentReason() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(reasonsRepository.count() + 1, POSTS);

        // when/then
        testSaveContentReportsThrowsIllegalArgumentException(requestDto, REASON_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveContentReportsWithEmptyOtherReason() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(reasonsRepository.count(), POSTS);

        // when/then
        testSaveContentReportsThrowsIllegalArgumentException(requestDto, EMPTY_OTHER_REASON);
    }

    @Test
    @WithMockUser(username = "username1")
    public void savePostReportsWithNonExistentPost() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(ID, POSTS);

        // when/then
        testSaveContentReportsThrowsIllegalArgumentException(requestDto, POST_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveCommentReportsWithNonExistentComment() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(ID, COMMENTS);

        // when/then
        testSaveContentReportsThrowsIllegalArgumentException(requestDto, COMMENT_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveUserReportsWithNonExistentUser() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(ID, USERS);

        // when/then
        testSaveContentReportsThrowsIllegalArgumentException(requestDto, USER_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveContentReportsWithEmptyType() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(ID, null);

        // when/then
        testSaveContentReportsThrowsIllegalArgumentException(requestDto, EMPTY_TYPE);
    }

    @Test
    @WithAnonymousUser
    public void deleteSelectedReportSummariesWithAnonymousUser() throws Exception {
        // given
        ContentReportSummaryDeleteDto requestDto = createContentReportSummaryDeleteDto(null);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/content/selected-delete");
        performAndExpectNotBadRequest(requestBuilder, requestDto);
    }
    @Test
    @WithMockUser(username = "username2", roles = "ADMIN")
    public void deleteSelectedReportSummariesWithEmptyReportIds() throws Exception {
        testDeleteSelectedReportSummariesException(null, EMPTY_CONTENT_IDS);
    }

    @Test
    @WithMockUser(username = "username2", roles = "ADMIN")
    public void deleteSelectedPostReportsSummariesWithNonExistentPost() throws Exception {
        testDeleteSelectedReportSummariesException(POSTS_ID, POST_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username2", roles = "ADMIN")
    public void deleteSelectedCommentReportSummariesWithNonExistentComment() throws Exception {
        testDeleteSelectedReportSummariesException(COMMENTS_ID, COMMENT_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username2", roles = "ADMIN")
    public void deleteSelectedUserReportSummariesWithNonExistentUser() throws Exception {
        testDeleteSelectedReportSummariesException(USER_ID, USER_NOT_FOUND);
    }

    @Test
    @WithAnonymousUser
    public void banUserWithAnonymousUser() throws Exception {
        // given
        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "process");
        performAndExpectNotBadRequest(requestBuilder, ID);
    }

    @Test
    @WithMockUser(username = "username2", roles = "ADMIN")
    public void banUserWithNonExistentUser() throws Exception {
        testBanUserException(USER_NOT_FOUND, userRepository.count() + 1);
    }

    @Test
    @WithMockUser(username = "username2", roles = "ADMIN")
    public void banUserWithNonExistentUserReportSummary() throws Exception {
        testBanUserException(REPORT_SUMMARY_NOT_FOUND, ID);
    }

    private ContentReportsSaveDto createContentReportsSaveDto(Long reasonId, ContentType contentType) {
        ContentReportsSaveDto requestDto = new ContentReportsSaveDto();
        requestDto.setReasonsId(reasonId);

        if (contentType != null) {
            switch (contentType) {
                case POSTS -> requestDto.setPostsId(ID);
                case COMMENTS -> requestDto.setCommentsId(ID);
                case USERS -> requestDto.setUserId(userRepository.count() + 1);
            }
        }

        return requestDto;
    }

    private ContentReportSummaryDeleteDto createContentReportSummaryDeleteDto(Fields field) {
        List<Long> postsId = Collections.emptyList();
        List<Long> commentsId = Collections.emptyList();
        List<Long> userId = Collections.emptyList();

        List<Long> value = Collections.singletonList(ID);
        if (POSTS_ID.equals(field)) {
            postsId = value;
        } else if (COMMENTS_ID.equals(field)) {
            commentsId = value;
        } else if (USER_ID.equals(field)) {
            userId = Collections.singletonList(userRepository.count() + 1);
        }

        return ContentReportSummaryDeleteDto.builder().postsId(postsId).commentsId(commentsId).userId(userId).build();
    }

    private void testSaveContentReportsThrowsMethodArgumentNotValidException(ContentReportsSaveDto requestDto,
                                                                             ResultMatcher[] resultMatchers) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/content");
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private void testSaveContentReportsThrowsIllegalArgumentException(ContentReportsSaveDto requestDto,
                                                                      ExceptionMessages exceptionMessage) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/content");
        ResultMatcher resultMatchers = createResultMatcher(exceptionMessage);
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private void testDeleteSelectedReportSummariesException(Fields field, ExceptionMessages exceptionMessage) throws Exception {
        // given
        ContentReportSummaryDeleteDto requestDto = createContentReportSummaryDeleteDto(field);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/content/selected-delete");
        ResultMatcher resultMatcher = createResultMatcher(exceptionMessage);
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatcher);
    }

    private void testBanUserException(ExceptionMessages exceptionMessage, Long requestDto) throws Exception {
        // given
        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/process");
        ResultMatcher resultMatcher = createResultMatcher(exceptionMessage);
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatcher);
    }

    private ResultMatcher[] createResultMatchers(Fields field, String rejectedValue, ExceptionMessages exceptionMessage) {
        return mockMvcTestHelper.createResultMatchers(field, rejectedValue, exceptionMessage);
    }

    private ResultMatcher createResultMatcher(ExceptionMessages exceptionMessage) {
        return mockMvcTestHelper.createResultMatcher(exceptionMessage);
    }

    private <Dto> void performAndExpectNotBadRequest(MockHttpServletRequestBuilder requestBuilder, Dto requestDto) throws Exception {
        mockMvcTestHelper.performAndExpectUnauthorized(requestBuilder, requestDto);
    }

    private <Dto> void performAndExpectBadRequest(MockHttpServletRequestBuilder requestBuilder, Dto requestDto,
                                                  ResultMatcher... resultMatchers) throws Exception {
        mockMvcTestHelper.performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private void assertOptionalAndGetEntity(Long contentId) {
        testDataHelper.assertOptionalAndGetEntity(userRepository::findById, contentId);
    }
}
