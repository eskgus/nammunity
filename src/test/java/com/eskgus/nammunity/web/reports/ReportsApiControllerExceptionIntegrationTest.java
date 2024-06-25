package com.eskgus.nammunity.web.reports;

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
        ContentReportsSaveDto requestDto = createContentReportsSaveDtoWithPostId(ID);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/content");
        performAndExpectNotBadRequest(requestBuilder, requestDto);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveContentReportsWithEmptyReasonsId() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDtoWithPostId(null);

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(REASONS_ID, null, EMPTY_REASON_ID);
        testSaveContentReportsThrowsMethodArgumentNotValidException(requestDto, resultMatchers);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveContentReportsWithInvalidOtherReasonsLength() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDtoWithPostId(ID);
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
        ContentReportsSaveDto requestDto = createContentReportsSaveDtoWithPostId(ID);

        // when/then
        testSaveContentReportsThrowsIllegalArgumentException(requestDto, USERNAME_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveContentReportsWithNonExistentReasonsId() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDtoWithPostId(reasonsRepository.count() + 1);

        // when/then
        testSaveContentReportsThrowsIllegalArgumentException(requestDto, REASON_ID_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveContentReportsWithEmptyOtherReasons() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDtoWithPostId(reasonsRepository.count());

        // when/then
        testSaveContentReportsThrowsIllegalArgumentException(requestDto, EMPTY_OTHER_REASON);
    }

    @Test
    @WithMockUser(username = "username1")
    public void savePostReportsWithNonExistentPostId() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDtoWithPostId(ID);

        // when/then
        testSaveContentReportsThrowsIllegalArgumentException(requestDto, POST_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveCommentReportsWithNonExistentCommentId() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(ID);
        requestDto.setCommentsId(ID);

        // when/then
        testSaveContentReportsThrowsIllegalArgumentException(requestDto, COMMENT_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveUserReportsWithNonExistentUserId() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(ID);
        requestDto.setUserId(userRepository.count() + 1);

        // when/then
        testSaveContentReportsThrowsIllegalArgumentException(requestDto, USER_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username1")
    public void saveContentReportsWithEmptyType() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(ID);

        // when/then
        testSaveContentReportsThrowsIllegalArgumentException(requestDto, EMPTY_TYPE);
    }

    @Test
    @WithAnonymousUser
    public void deleteSelectedContentReportsWithAnonymousUser() throws Exception {
        // given
        ContentReportSummaryDeleteDto requestDto = createContentReportSummaryDeleteDto(null);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/content/selected-delete");
        performAndExpectNotBadRequest(requestBuilder, requestDto);
    }
    @Test
    @WithMockUser(username = "username2", roles = "ADMIN")
    public void deleteSelectedContentReportsWithEmptyReportIds() throws Exception {
        testDeleteSelectedContentReportsException(null, EMPTY_CONTENT_IDS);
    }

    @Test
    @WithMockUser(username = "username2", roles = "ADMIN")
    public void deleteSelectedPostReportsWithNonExistentPostId() throws Exception {
        testDeleteSelectedContentReportsException(POSTS_ID, POST_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username2", roles = "ADMIN")
    public void deleteSelectedCommentReportsWithNonExistentCommentId() throws Exception {
        testDeleteSelectedContentReportsException(COMMENTS_ID, COMMENT_NOT_FOUND);
    }

    @Test
    @WithMockUser(username = "username2", roles = "ADMIN")
    public void deleteSelectedUserReportsWithNonExistentUserId() throws Exception {
        testDeleteSelectedContentReportsException(USER_ID, USER_NOT_FOUND);
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
    public void banUserWithNonExistentUserId() throws Exception {
        testBanUserException(USER_NOT_FOUND, userRepository.count() + 1);
    }

    @Test
    @WithMockUser(username = "username2", roles = "ADMIN")
    public void banUserWithNonExistentUserReportSummary() throws Exception {
        testBanUserException(USER_REPORT_SUMMARY_NOT_FOUND, ID);
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

    private void testDeleteSelectedContentReportsException(Fields field, ExceptionMessages exceptionMessage) throws Exception {
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

    private ContentReportsSaveDto createContentReportsSaveDtoWithPostId(Long reasonId) {
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(reasonId);
        requestDto.setPostsId(ID);

        return requestDto;
    }

    private ContentReportsSaveDto createContentReportsSaveDto(Long reasonId) {
        ContentReportsSaveDto requestDto = new ContentReportsSaveDto();
        requestDto.setReasonsId(reasonId);

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

    private ResultMatcher[] createResultMatchers(Fields field, String rejectedValue, ExceptionMessages exceptionMessage) {
        return mockMvcTestHelper.createResultMatchers(field, rejectedValue, exceptionMessage);
    }

    private ResultMatcher createResultMatcher(ExceptionMessages exceptionMessage) {
        return mockMvcTestHelper.createResultMatcher(exceptionMessage);
    }

    private <T> void performAndExpectNotBadRequest(MockHttpServletRequestBuilder requestBuilder, T requestDto) throws Exception {
        mockMvcTestHelper.performAndExpectUnauthorized(requestBuilder, requestDto);
    }

    private <T> void performAndExpectBadRequest(MockHttpServletRequestBuilder requestBuilder, T requestDto,
                                                ResultMatcher... resultMatchers) throws Exception {
        mockMvcTestHelper.performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private void assertOptionalAndGetEntity(Long contentId) {
        testDataHelper.assertOptionalAndGetEntity(userRepository::findById, contentId);
    }
}
