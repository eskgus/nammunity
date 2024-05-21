package com.eskgus.nammunity.web.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.util.TestDB;
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

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportsApiControllerTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private ContentReportSummaryRepository reportSummaryRepository;

    private User user1;
    private User user2;
    private Posts post;
    private Comments comment;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        this.user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDB.signUp(2L, Role.ADMIN);
        this.user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        Long postId = testDB.savePosts(user1);
        this.post = assertOptionalAndGetEntity(postsRepository::findById, postId);

        Long commentId = testDB.saveComments(postId, user1);
        this.comment = assertOptionalAndGetEntity(commentsRepository::findById, commentId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDB.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    @WithMockUser(username = "username2")
    public void saveContentReports() throws Exception {
        // 1. 게시글 신고
        saveContentReport(post.getId(), ContentType.POSTS);

        // 2. 댓글 신고
        saveContentReport(comment.getId(), ContentType.COMMENTS);

        // 3. 사용자 신고
        saveContentReport(user1.getId(), ContentType.USERS);
    }

    private void saveContentReport(Long contentId, ContentType contentType) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/api/reports/content");
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(contentId, contentType);

        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);
    }

    private ContentReportsSaveDto createContentReportsSaveDto(Long contentId, ContentType contentType) {
        ContentReportsSaveDto requestDto = new ContentReportsSaveDto();
        requestDto.setReasonsId(1L);
        switch (contentType) {
            case POSTS -> requestDto.setPostsId(contentId);
            case COMMENTS -> requestDto.setCommentsId(contentId);
            case USERS -> requestDto.setUserId(contentId);
        }
        return requestDto;
    }

    @Test
    @WithMockUser(username = "username2", roles = {"ADMIN"})
    public void deleteSelectedContentReports() throws Exception {
        // 1. 게시글
        List<Long> postIds = savePostReportSummaries();
        deleteSelectedReportSummaries(postIds, ContentType.POSTS);

        // 2. 댓글
        List<Long> commentIds = saveCommentReportSummaries();
        deleteSelectedReportSummaries(commentIds, ContentType.COMMENTS);

        // 3. 사용자
        List<Long> userIds = saveUserReportSummaries();
        deleteSelectedReportSummaries(userIds, ContentType.USERS);
    }

    private List<Long> savePostReportSummaries() {
        List<Long> postIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Long postId = testDB.savePosts(user1);
            Posts post = assertOptionalAndGetEntity(postsRepository::findById, postId);
            saveReportSummary(testDB::savePostReportSummary, post);
            postIds.add(postId);
        }
        return postIds;
    }

    private <T> Long saveReportSummary(BiFunction<T, User, Long> reportSummarySaver, T content) {
        Long reportSummaryId = reportSummarySaver.apply(content, user2);
        assertOptionalAndGetEntity(reportSummaryRepository::findById, reportSummaryId);
        return reportSummaryId;
    }

    private List<Long> saveCommentReportSummaries() {
        List<Long> commentIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Long commentId = testDB.saveComments(post.getId(), user1);
            Comments comment = assertOptionalAndGetEntity(commentsRepository::findById, commentId);
            saveReportSummary(testDB::saveCommentReportSummary, comment);
            commentIds.add(commentId);
        }
        return commentIds;
    }

    private List<Long> saveUserReportSummaries() {
        List<Long> userIds = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            Long userId = testDB.signUp(i + user2.getId(), Role.USER);
            User user = assertOptionalAndGetEntity(userRepository::findById, userId);
            saveReportSummary(testDB::saveUserReportSummary, user);
            userIds.add(userId);
        }
        return userIds;
    }

    private void deleteSelectedReportSummaries(List<Long> contentIds, ContentType contentType) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = delete("/api/reports/content/selected-delete");
        ContentReportSummaryDeleteDto requestDto = createContentReportSummaryDeleteDto(contentIds, contentType);

        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);
    }

    private ContentReportSummaryDeleteDto createContentReportSummaryDeleteDto(List<Long> contentIds, ContentType contentType) {
        List<Long> postIds = contentType.equals(ContentType.POSTS) ? contentIds : Collections.emptyList();
        List<Long> commentIds = contentType.equals(ContentType.COMMENTS) ? contentIds : Collections.emptyList();
        List<Long> userIds = contentType.equals(ContentType.USERS) ? contentIds : Collections.emptyList();

        return ContentReportSummaryDeleteDto.builder().postsId(postIds).commentsId(commentIds).userId(userIds).build();
    }

    @Test
    @WithMockUser(username = "username2", roles = {"ADMIN"})
    public void banUser() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/api/reports/process");
        Long requestDto = saveReportSummary(testDB::saveUserReportSummary, user1);

        mockMvcTestHelper.requestAndAssertStatusIsOk(requestBuilder, requestDto);
    }
}
