package com.eskgus.nammunity.web.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.*;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDeleteDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportsSaveDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Period;
import java.util.*;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportsApiControllerIntegrationTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private ContentReportsRepository reportsRepository;

    @Autowired
    private ContentReportSummaryRepository reportSummaryRepository;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    private User user;
    private Posts post;
    private Comments comment;
    private User reporter;

    private static final String REQUEST_MAPPING = "/api/reports";

    @BeforeEach
    public void setUp() {
        Long user1Id = testDataHelper.signUp(1L, Role.USER);
        this.user = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDataHelper.signUp(2L, Role.USER);
        this.reporter = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        Long user3Id = testDataHelper.signUp(3L, Role.ADMIN);
        assertOptionalAndGetEntity(userRepository::findById, user3Id);

        Long postId = testDataHelper.savePosts(user);
        this.post = assertOptionalAndGetEntity(postsRepository::findById, postId);

        Long commentId = testDataHelper.saveComments(postId, user);
        this.comment = assertOptionalAndGetEntity(commentsRepository::findById, commentId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    @WithMockUser(username = "username2")
    public void savePostReportsOnly() throws Exception {
        tesSavePostReports();
    }

    @Test
    @WithMockUser(username = "username2")
    public void saveCommentReportsOnly() throws Exception {
        testSaveCommentReports();
    }

    @Test
    @WithMockUser(username = "username2")
    public void saveUserReportsOnly() throws Exception {
        testSaveUserReports();
    }

    @Test
    @WithMockUser(username = "username2")
    public void savePostReportAndSummary() throws Exception {
        testSavePostReportAndSummary();
    }

    @Test
    @WithMockUser(username = "username2")
    public void saveCommentReportAndSummary() throws Exception {
        testSaveCommentReportAndSummary();
    }

    @Test
    @WithMockUser(username = "username2")
    public void saveUserReportAndSummary() throws Exception {
        testSaveUserReportAndSummary();
    }

    @Test
    @WithMockUser(username = "username2")
    public void updatePostReportSummary() throws Exception {
        savePostReportSummary();
        testSavePostReportAndSummary();
    }

    @Test
    @WithMockUser(username = "username2")
    public void updateCommentReportSummary() throws Exception {
        saveCommentReportSummary();
        testSaveCommentReportAndSummary();
    }

    @Test
    @WithMockUser(username = "username2")
    public void updateUserReportSummary() throws Exception {
        saveUserReportSummary();
        testSaveUserReportAndSummary();
    }

    @Test
    @WithMockUser(username = "username3", roles = "ADMIN")
    public void deleteSelectedContentReports() throws Exception {
        // given
        saveReportSummaries();

        ContentReportSummaryDeleteDto requestDto = createContentReportSummaryDeleteDto();

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/content/selected-delete");
        performAndExpectOk(requestBuilder, requestDto);
    }

    @Test
    @WithMockUser(username = "username3", roles = "ADMIN")
    public void saveBanUser() throws Exception {
        // given
        saveUserReportSummary();

        // when/then
        testBanUser();
    }

    @Test
    @WithMockUser(username = "username3", roles = "ADMIN")
    public void updateBanUser() throws Exception {
        // given
        saveBanedUser(Period.ofWeeks(1));

        // when/then
        testBanUser();
    }

    private void testSavePostReportAndSummary() throws Exception {
        savePostReports();
        tesSavePostReports();
    }

    private void testSaveCommentReportAndSummary() throws Exception {
        saveCommentReports();
        testSaveCommentReports();
    }

    private void testSaveUserReportAndSummary() throws Exception {
        saveUserReports();
        testSaveUserReports();
    }

    private void tesSavePostReports() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDtoWithPostId();

        // when/then
        testSaveContentReports(requestDto);
    }

    private void testSaveCommentReports() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDtoWithCommentId();

        // when/then
        testSaveContentReports(requestDto);
    }

    private void testSaveUserReports() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDtoWithUserId();

        // when/then
        testSaveContentReports(requestDto);
    }

    private void testSaveContentReports(ContentReportsSaveDto requestDto) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/content");
        performAndExpectOk(requestBuilder, requestDto);
    }

    private void testBanUser() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/process");
        performAndExpectOk(requestBuilder, user.getId());
    }

    private <T> void performAndExpectOk(MockHttpServletRequestBuilder requestBuilder, T requestDto) throws Exception {
        mockMvcTestHelper.performAndExpectOk(requestBuilder, requestDto);
    }

    private void savePostReports() {
        Long latestPostReportId = testDataHelper.savePostReports(post.getId(), reporter);
        assertOptionalAndGetEntity(reportsRepository::findById, latestPostReportId);
    }

    private void saveCommentReports() {
        Long latestCommentReportId = testDataHelper.saveCommentReports(comment.getId(), reporter);
        assertOptionalAndGetEntity(reportsRepository::findById, latestCommentReportId);
    }

    private void saveUserReports() {
        Long latestUserReportId = testDataHelper.saveUserReports(user, reporter);
        assertOptionalAndGetEntity(reportsRepository::findById, latestUserReportId);
    }

    private void saveReportSummaries() {
        savePostReportSummary();
        saveCommentReportSummary();
        saveUserReportSummary();
    }

    private void saveBanedUser(Period period) {
        saveUserReportSummary();
        Long bannedUserId = testDataHelper.saveBannedUsers(user, period);
        assertOptionalAndGetEntity(bannedUsersRepository::findById, bannedUserId);
    }

    private void savePostReportSummary() {
        Long postReportSummaryId = testDataHelper.savePostReportSummary(post, reporter);
        assertOptionalAndGetEntity(reportSummaryRepository::findById, postReportSummaryId);
    }

    private void saveCommentReportSummary() {
        Long commentReportSummaryId = testDataHelper.saveCommentReportSummary(comment, reporter);
        assertOptionalAndGetEntity(reportSummaryRepository::findById, commentReportSummaryId);
    }

    private void saveUserReportSummary() {
        Long userReportSummaryId = testDataHelper.saveUserReportSummary(user, reporter);
        assertOptionalAndGetEntity(reportSummaryRepository::findById, userReportSummaryId);
    }

    private ContentReportsSaveDto createContentReportsSaveDtoWithPostId() {
        ContentReportsSaveDto requestDto = createContentReportsSaveDto();
        requestDto.setPostsId(post.getId());

        return requestDto;
    }

    private ContentReportsSaveDto createContentReportsSaveDtoWithCommentId() {
        ContentReportsSaveDto requestDto = createContentReportsSaveDto();
        requestDto.setCommentsId(comment.getId());

        return requestDto;
    }

    private ContentReportsSaveDto createContentReportsSaveDtoWithUserId() {
        ContentReportsSaveDto requestDto = createContentReportsSaveDto();
        requestDto.setUserId(user.getId());

        return requestDto;
    }

    private ContentReportsSaveDto createContentReportsSaveDto() {
        ContentReportsSaveDto requestDto = new ContentReportsSaveDto();
        requestDto.setReasonsId(1L);

        return requestDto;
    }

    private ContentReportSummaryDeleteDto createContentReportSummaryDeleteDto() {
        List<Long> postsId = Collections.singletonList(post.getId());
        List<Long> commentsId = Collections.singletonList(comment.getId());
        List<Long> userId = Collections.singletonList(user.getId());

        return ContentReportSummaryDeleteDto.builder()
                .postsId(postsId).commentsId(commentsId).userId(userId).build();
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }
}
