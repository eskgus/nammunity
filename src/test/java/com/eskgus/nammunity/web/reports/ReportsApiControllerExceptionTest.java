package com.eskgus.nammunity.web.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.ReasonsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReportsApiControllerExceptionTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReasonsRepository reasonsRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    private User user1;
    private Posts post;
    private Comments comment;

    private MockHttpServletRequestBuilder requestBuilder;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        this.user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDB.signUp(2L, Role.ADMIN);
        assertOptionalAndGetEntity(userRepository::findById, user2Id);

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
    @WithMockUser(username = "username1")
    public void saveContentReportsExceptions() throws Exception {
        this.requestBuilder = post("/api/reports/content");

        // 예외 1. 신고 사유 선택 x
        saveContentReportsWithEmptyReasonId();

        // 예외 2. 기타 사유 입력 x
        requestAndAssertSaveContentReportsExceptions(reasonsRepository.count(), post.getId(), ContentType.POSTS,
                "기타 사유를 입력하세요.");

        // 예외 3. 게시글 존재 x
        requestAndAssertSaveContentReportsExceptions(1L, post.getId() + 1, ContentType.POSTS,
                "해당 게시글이 없습니다.");

        // 예외 4. 댓글 존재 x
        requestAndAssertSaveContentReportsExceptions(1L, comment.getId() + 1, ContentType.COMMENTS,
                "해당 댓글이 없습니다.");

        // 예외 5. 사용자 존재 x
        requestAndAssertSaveContentReportsExceptions(1L, userRepository.count() + 1, ContentType.USERS,
                "존재하지 않는 회원입니다.");

        // 예외 6. 신고할 컨텐츠 (신고 분류) 선택 x
        requestAndAssertSaveContentReportsExceptions(1L, null, ContentType.POSTS,
                "신고 분류가 선택되지 않았습니다.");
    }

    private void saveContentReportsWithEmptyReasonId() throws Exception {
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(null, post.getId(), ContentType.POSTS);
        ResultMatcher[] resultMatchers = mockMvcTestHelper.createResultMatchers("reasonsId",
                "신고 사유를 선택하세요.");

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatchers);
    }

    private ContentReportsSaveDto createContentReportsSaveDto(Long reasonId, Long contentId, ContentType contentType) {
        ContentReportsSaveDto requestDto = new ContentReportsSaveDto();
        requestDto.setReasonsId(reasonId);
        switch (contentType) {
            case POSTS -> requestDto.setPostsId(contentId);
            case COMMENTS -> requestDto.setCommentsId(contentId);
            case USERS -> requestDto.setUserId(contentId);
        }
        return requestDto;
    }

    private void requestAndAssertSaveContentReportsExceptions(Long reasonId, Long contentId, ContentType contentType,
                                                              String expectedContent) throws Exception {
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(reasonId, contentId, contentType);
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher(expectedContent);

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatcher);
    }

    @Test
    @WithMockUser(username = "username2", roles = {"ADMIN"})
    public void deleteSelectedContentReportsExceptions() throws Exception {
        this.requestBuilder = delete("/api/reports/content/selected-delete");

        List<Long> emptyList = new ArrayList<>();
        List<Long> contentIds = createContentIds();

        // 예외 1. 삭제할 항목 선택 x
        requestAndAssertDeleteSelectedContentReportsExceptions(emptyList, emptyList, emptyList,
                "삭제할 항목을 선택하세요.");

        // 예외 2. 게시글 존재 x
        requestAndAssertDeleteSelectedContentReportsExceptions(contentIds, emptyList, emptyList,
                "해당 게시글이 없습니다.");

        // 예외 3. 댓글 존재 x
        requestAndAssertDeleteSelectedContentReportsExceptions(emptyList, contentIds, emptyList,
                "해당 댓글이 없습니다.");

        // 예외 4. 사용자 존재 x
        requestAndAssertDeleteSelectedContentReportsExceptions(emptyList, emptyList, contentIds,
                "존재하지 않는 회원입니다.");
    }

    private void requestAndAssertDeleteSelectedContentReportsExceptions(List<Long> postIds, List<Long> commentIds,
                                                                        List<Long> userIds, String expectedContent) throws Exception {
        ContentReportSummaryDeleteDto requestDto = createContentReportSummaryDeleteDto(postIds, commentIds, userIds);
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher(expectedContent);

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatcher);
    }

    private ContentReportSummaryDeleteDto createContentReportSummaryDeleteDto(List<Long> postIds, List<Long> commentIds,
                                                                              List<Long> userIds) {
        return ContentReportSummaryDeleteDto.builder()
                .postsId(postIds).commentsId(commentIds).userId(userIds).build();
    }

    private List<Long> createContentIds() {
        List<Long> contentIds = new ArrayList<>();
        for (long i = 1; i <= 10; i++) {
            contentIds.add(i);
        }
        return contentIds;
    }

    @Test
    @WithMockUser(username = "username2", roles = {"ADMIN"})
    public void banUserExceptions() throws Exception {
        this.requestBuilder = post("/api/reports/process");

        // 예외 1. 사용자 존재 x
        requestAndAssertBanUserExceptions(userRepository.count() + 1, "존재하지 않는 회원입니다.");

        // 예외 2. 신고 내역 존재 x
        requestAndAssertBanUserExceptions(user1.getId(), "신고 요약 내역이 존재하지 않는 회원입니다.");
    }

    private void requestAndAssertBanUserExceptions(Long requestDto, String expectedContent) throws Exception {
        ResultMatcher resultMatcher = mockMvcTestHelper.createResultMatcher(expectedContent);

        mockMvcTestHelper.requestAndAssertStatusIsBadRequest(requestBuilder, requestDto, resultMatcher);
    }
}
