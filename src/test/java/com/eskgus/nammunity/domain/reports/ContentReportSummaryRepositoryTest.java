package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.service.reports.TypesService;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

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

    private User user1;
    private User user2;
    private Posts post;
    private Comments comment;

    private Long userReportSummaryId;

    @BeforeEach
    public void setUp() {
        // 1. user1 회원가입 + user2 회원가입
        Long user1Id = testDB.signUp(1L, Role.USER);
        Long user2Id = testDB.signUp(2L, Role.USER);
        Assertions.assertThat(userRepository.count()).isEqualTo(user2Id);

        this.user1 = userRepository.findById(user1Id).get();
        this.user2 = userRepository.findById(user2Id).get();

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(this.user1);
        Assertions.assertThat(postsRepository.count()).isEqualTo(postId);

        this.post = postsRepository.findById(postId).get();

        // 3. user1이 댓글 작성
        Long commentId = testDB.saveComments(postId, this.user1);
        Assertions.assertThat(commentsRepository.count()).isEqualTo(commentId);

        this.comment = commentsRepository.findById(commentId).get();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void existsByContents() {
        // 1. user1 회원가입 + user2 회원가입
        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        // 4. 신고 요약 저장 x 후 existsByContents() 호출
        callAndAssertExistsByContents(this.post, false);
        callAndAssertExistsByContents(this.comment, false);
        callAndAssertExistsByContents(this.user1, false);

        // 5. 신고 요약 저장 후 existsByContents() 호출
        saveReportSummaries();

        callAndAssertExistsByContents(this.post, true);
        callAndAssertExistsByContents(this.comment, true);
        callAndAssertExistsByContents(this.user1, true);
    }

    private <T> void callAndAssertExistsByContents(T contents, boolean expectedResult) {
        boolean doesReportSummaryExist = contentReportSummaryRepository.existsByContents(contents);
        Assertions.assertThat(doesReportSummaryExist).isEqualTo(expectedResult);
    }

    private void saveReportSummaries() {
        testDB.savePostReportSummary(this.post, this.user2);
        testDB.saveCommentReportSummary(this.comment, this.user2);
        this.userReportSummaryId = testDB.saveUserReportSummary(this.user1, this.user2);
        Assertions.assertThat(contentReportSummaryRepository.count()).isEqualTo(this.userReportSummaryId);
    }

    @Test
    public void findByContents() {
        // 1. user1 회원가입 + user2 회원가입
        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        // 4. 신고 요약 저장 후 findByContents() 호출
        saveReportSummaries();

        callAndAssertFindByContents(this.post);
        callAndAssertFindByContents(this.comment);
        callAndAssertFindByContents(this.user1);
    }

    private <T> void callAndAssertFindByContents(T contents) {
        ContentReportSummary reportSummary = contentReportSummaryRepository.findByContents(contents);
        Assertions.assertThat(reportSummary).isNotNull();
        assertContentId(reportSummary, contents);
    }

    private <T> void assertContentId(ContentReportSummary reportSummary, T contents) {
        Long actualId = getActualId(reportSummary);
        Long expectedId = getExpectedId(contents);
        Assertions.assertThat(actualId).isEqualTo(expectedId);
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
        // 1. user1 회원가입 + user2 회원가입
        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        // 4. 신고 요약 저장 후 findAllDesc() 호출
        saveReportSummaries();

        List<ContentReportSummaryDto> summaryDtos = contentReportSummaryRepository.findAllDesc();

        Assertions.assertThat(summaryDtos.size()).isEqualTo(this.userReportSummaryId.intValue());
        Assertions.assertThat(summaryDtos.get(0).getUserId()).isEqualTo(this.user1.getId());
        Assertions.assertThat(summaryDtos.get(1).getCommentId()).isEqualTo(this.comment.getId());
        Assertions.assertThat(summaryDtos.get(2).getPostId()).isEqualTo(this.post.getId());
    }

    @Test
    public void findByTypes() {
        saveReportSummaries();

        callAndAssertFindByTypes(Posts.class);
        callAndAssertFindByTypes(Comments.class);
        callAndAssertFindByTypes(User.class);
    }

    private <T> void callAndAssertFindByTypes(Class<T> classOfType) {
        Types type = typesService.findByClass(classOfType);
        List<ContentReportSummaryDto> summaryDtos = contentReportSummaryRepository.findByTypes(type);
        Assertions.assertThat(summaryDtos.size()).isOne();

        ContentReportSummaryDto summaryDto = summaryDtos.get(0);
        Assertions.assertThat(summaryDto.getType()).isEqualTo(type.getDetail());
    }
}
