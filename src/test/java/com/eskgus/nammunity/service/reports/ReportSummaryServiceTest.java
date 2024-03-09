package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummarySaveDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReportSummaryServiceTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private TypesRepository typesRepository;

    @Autowired
    private ReasonsRepository reasonsRepository;

    @Autowired
    private ContentReportSummaryRepository contentReportSummaryRepository;

    @Autowired
    private ReportSummaryService reportSummaryService;

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
    public void saveContentReportSummary() {
        // 1. user1 회원가입 + user2 회원가입
        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        // 4. ContentReportSummarySaveDto로 saveContentReportSummary() 호출
        // 4-1. posts = post, reporter = user2로 summarySaveDto 생성
        callAndAssertSaveContentReportSummary(this.post);

        // 4-2. comments = comment, reporter = user2로 summarySaveDto 생성
        callAndAssertSaveContentReportSummary(this.comment);

        // 4-3. user = user1, reporter = user2로 summarySaveDto 생성
        callAndAssertSaveContentReportSummary(this.user1);
    }

    private <T> void callAndAssertSaveContentReportSummary(T contents) {
        // 1. contents로 ContentReportSummarySaveDto 생성
        ContentReportSummarySaveDto summarySaveDto = createSummarySaveDto(contents);

        // 2. summarySaveDto로 saveContentReportSummary() 호출
        Long id = reportSummaryService.saveContentReportSummary(summarySaveDto);

        // 3. 검증
        Optional<ContentReportSummary> result = contentReportSummaryRepository.findById(id);
        Assertions.assertThat(result).isPresent();

        ContentReportSummary reportSummary = result.get();
        Assertions.assertThat(reportSummary.getReporter().getId()).isEqualTo(this.user2.getId());
        assertContentId(reportSummary, contents);
    }

    private <T> ContentReportSummarySaveDto createSummarySaveDto(T contents) {
        Posts post = null;
        Comments comment = null;
        User user = null;
        Types type;
        Reasons reason = reasonsRepository.findById(reasonsRepository.count()).get();

        if (contents instanceof Posts) {
            post = (Posts) contents;
            type = typesRepository.findById(1L).get();
        } else if (contents instanceof Comments) {
            comment = (Comments) contents;
            type = typesRepository.findById(2L).get();
        } else {
            user = (User) contents;
            type = typesRepository.findById(3L).get();
        }

        return ContentReportSummarySaveDto.builder()
                .posts(post).comments(comment).user(user)
                .types(type).reportedDate(LocalDateTime.now()).reporter(this.user2)
                .reasons(reason).otherReasons("기타 사유")
                .build();
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
    public void updateContentReportSummary() {
        // 1. user1 회원가입 + user2 회원가입
        // 2. user1이 게시글 작성
        // 3. user1이 댓글 작성
        // 4. 신고 요약 저장
        saveReportSummaries();

        // 5. ContentReportSummarySaveDto, contents(post, comment, user)로 updateContentReportSummary() 호출
        // 5-1. contents = post
        callAndAssertUpdateContentReportSummary(this.post);

        // 5-2. contents = comment
        callAndAssertUpdateContentReportSummary(this.comment);

        // 5-3. contents = user2
        callAndAssertUpdateContentReportSummary(this.user1);
    }

    private void saveReportSummaries() {
        testDB.savePostReportSummary(this.post, this.user2);
        testDB.saveCommentReportSummary(this.comment, this.user2);
        this.userReportSummaryId = testDB.saveUserReportSummary(this.user1, this.user2);
        Assertions.assertThat(contentReportSummaryRepository.count()).isEqualTo(this.userReportSummaryId);
    }

    private <T> void callAndAssertUpdateContentReportSummary(T contents) {
        ContentReportSummary reportSummary = contentReportSummaryRepository.findByContents(contents);

        // 1. contents로 ContentReportSummarySaveDto 생성
        ContentReportSummarySaveDto summarySaveDto = createSummarySaveDto(contents);

        // 2. summarySaveDto, contents로 updateContentReportSummary() 호출
        Long id = reportSummaryService.updateContentReportSummary(summarySaveDto, contents);

        // 3. 검증
        Optional<ContentReportSummary> result = contentReportSummaryRepository.findById(id);
        Assertions.assertThat(result).isPresent();

        ContentReportSummary updatedReportSummary = result.get();
        Assertions.assertThat(updatedReportSummary.getReportedDate().isAfter(reportSummary.getReportedDate())).isTrue();
    }

    @Test
    public void saveOrUpdateContentReportSummary() {
        // 1. 신고 요약 저장 전 => 저장
        callAndAssertSaveOrUpdateContentReportSummary(this.post);
        callAndAssertSaveOrUpdateContentReportSummary(this.comment);
        callAndAssertSaveOrUpdateContentReportSummary(this.user1);

        // 2. 신고 요약 저장 후 => 업데이트
        callAndAssertSaveOrUpdateContentReportSummary(this.post);
        callAndAssertSaveOrUpdateContentReportSummary(this.comment);
        callAndAssertSaveOrUpdateContentReportSummary(this.user1);
    }

    private <T> void callAndAssertSaveOrUpdateContentReportSummary(T contents) {
        // 1. contents로 ContentReportSummarySaveDto 생성
        ContentReportSummarySaveDto summarySaveDto = createSummarySaveDto(contents);

        // 2. summarySaveDto로 saveOrUpdateContentReportSummary() 호출
        Long id = reportSummaryService.saveOrUpdateContentReportSummary(summarySaveDto);

        // 3. 검증
        Optional<ContentReportSummary> result = contentReportSummaryRepository.findById(id);
        Assertions.assertThat(result).isPresent();

        ContentReportSummary reportSummary = result.get();
        assertContentId(reportSummary, contents);
    }

    @Test
    public void findAllDesc() {
        saveReportSummaries();

        List<ContentReportSummaryDto> summaryDtos = reportSummaryService.findAllDesc();

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
        Types expectedType = typesService.findByClass(classOfType);
        List<ContentReportSummaryDto> summaryDtos = reportSummaryService.findByTypes(classOfType);
        Assertions.assertThat(summaryDtos.size()).isOne();

        ContentReportSummaryDto summaryDto = summaryDtos.get(0);
        Assertions.assertThat(summaryDto.getType()).isEqualTo(expectedType.getDetail());
    }
}
