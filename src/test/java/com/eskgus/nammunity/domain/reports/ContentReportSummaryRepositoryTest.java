package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.service.reports.TypesService;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ContentReportSummaryRepositoryTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private ContentReportSummaryRepository contentReportSummaryRepository;

    @Autowired
    private TypesService typesService;

    private User[] users;
    private Posts post;
    private Comments comment;

    private Long userReportSummaryId;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        Long user2Id = testDB.signUp(2L, Role.USER);
        assertThat(userRepository.count()).isEqualTo(user2Id);

        User user1 = userRepository.findById(user1Id).get();
        User user2 = userRepository.findById(user2Id).get();

        this.users = new User[]{ user1, user2 };

        Long postId = testDB.savePosts(user1);
        assertThat(postsRepository.count()).isEqualTo(postId);

        this.post = postsRepository.findById(postId).get();

        Long commentId = testDB.saveComments(postId, user1);
        assertThat(commentsRepository.count()).isEqualTo(commentId);

        this.comment = commentsRepository.findById(commentId).get();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void existsByContents() {
        // 1. 신고 요약 저장 x 후 호출
        callAndAssertExistsByContents(post, false);
        callAndAssertExistsByContents(comment, false);
        callAndAssertExistsByContents(users[0], false);

        // 2. 신고 요약 저장 후 호출
        saveReportSummaries();

        callAndAssertExistsByContents(post, true);
        callAndAssertExistsByContents(comment, true);
        callAndAssertExistsByContents(users[0], true);
    }

    private <T> void callAndAssertExistsByContents(T contents, boolean expectedResult) {
        boolean actualResult = contentReportSummaryRepository.existsByContents(contents);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private void saveReportSummaries() {
        testDB.savePostReportSummary(post, users[1]);
        testDB.saveCommentReportSummary(comment, users[1]);
        this.userReportSummaryId = testDB.saveUserReportSummary(users[0], users[1]);
        assertThat(contentReportSummaryRepository.count()).isEqualTo(userReportSummaryId);
    }

    @Test
    public void findByContents() {
        saveReportSummaries();

        callAndAssertFindByContents(post);
        callAndAssertFindByContents(comment);
        callAndAssertFindByContents(users[0]);
    }

    private <T> void callAndAssertFindByContents(T contents) {
        ContentReportSummary reportSummary = contentReportSummaryRepository.findByContents(contents);
        assertFindByContents(reportSummary, contents);
    }

    private <T> void assertFindByContents(ContentReportSummary reportSummary, T contents) {
        assertThat(reportSummary).isNotNull();
        assertContentId(reportSummary, contents);
    }

    private <T> void assertContentId(ContentReportSummary reportSummary, T contents) {
        Long actualId = getActualId(reportSummary);
        Long expectedId = getExpectedId(contents);
        assertThat(actualId).isEqualTo(expectedId);
    }

    private Long getActualId(ContentReportSummary reportSummary) {
        if (reportSummary.getTypes().getDetail().equals("게시글")) {
            return reportSummary.getPosts().getId();
        } else if (reportSummary.getTypes().getDetail().equals("댓글")) {
            return reportSummary.getComments().getId();
        } else {
            return reportSummary.getUser().getId();
        }
    }

    private <T> Long getExpectedId(T contents) {
        if (contents instanceof Posts) {
            return ((Posts) contents).getId();
        } else if (contents instanceof Comments) {
            return ((Comments) contents).getId();
        } else {
            return ((User) contents).getId();
        }
    }

    @Test
    public void findAllDesc() {
        saveReportSummaries();

        callAndAssertFindAllDesc();
    }

    private void callAndAssertFindAllDesc() {
        List<ContentReportSummaryDto> summaryDtos = contentReportSummaryRepository.findAllDesc();
        assertFindAllDesc(summaryDtos);
    }

    private void assertFindAllDesc(List<ContentReportSummaryDto> summaryDtos) {
        assertThat(summaryDtos.size()).isEqualTo(userReportSummaryId.intValue());
        assertThat(summaryDtos.get(0).getUserId()).isEqualTo(users[0].getId());
        assertThat(summaryDtos.get(1).getCommentId()).isEqualTo(comment.getId());
        assertThat(summaryDtos.get(2).getPostId()).isEqualTo(post.getId());
    }

    @Test
    public void findByTypes() {
        saveReportSummaries();

        callAndAssertFindByTypes(ContentType.POSTS);
        callAndAssertFindByTypes(ContentType.COMMENTS);
        callAndAssertFindByTypes(ContentType.USERS);
    }

    private void callAndAssertFindByTypes(ContentType contentType) {
        Types expectedType = typesService.findByContentType(contentType);
        List<ContentReportSummaryDto> summaryDtos = contentReportSummaryRepository.findByTypes(expectedType);

        assertFindByTypes(summaryDtos, expectedType);
    }

    private void assertFindByTypes(List<ContentReportSummaryDto> summaryDtos, Types expectedType) {
        assertThat(summaryDtos.size()).isOne();

        ContentReportSummaryDto summaryDto = summaryDtos.get(0);
        assertThat(summaryDto.getType()).isEqualTo(expectedType.getDetail());
    }

    @Test
    public void findByUser() {
        callAndAssertFindByUser(false);

        saveReportSummaries();
        callAndAssertFindByUser(true);    }

    private void callAndAssertFindByUser(boolean expectedResult) {
        Optional<ContentReportSummary> result = contentReportSummaryRepository.findByUser(users[0]);
        assertThat(result.isPresent()).isEqualTo(expectedResult);
    }
}
