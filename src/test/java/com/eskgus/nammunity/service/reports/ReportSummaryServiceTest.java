package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDeleteDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummarySaveDto;
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

import static org.assertj.core.api.Assertions.assertThat;

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

    private User[] users;
    private Posts post;
    private Comments comment;

    private Long userReportSummaryId;

    @BeforeEach
    public void setUp() {
        // 1. user1 회원가입 + user2 회원가입
        Long user1Id = testDB.signUp(1L, Role.USER);
        Long user2Id = testDB.signUp(2L, Role.USER);
        assertThat(userRepository.count()).isEqualTo(user2Id);

        User user1 = userRepository.findById(user1Id).get();
        User user2 = userRepository.findById(user2Id).get();

        this.users = new User[]{ user1, user2 };

        // 2. user1이 게시글 작성
        Long postId = testDB.savePosts(user1);
        assertThat(postsRepository.count()).isEqualTo(postId);

        this.post = postsRepository.findById(postId).get();

        // 3. user1이 댓글 작성
        Long commentId = testDB.saveComments(postId, user1);
        assertThat(commentsRepository.count()).isEqualTo(commentId);

        this.comment = commentsRepository.findById(commentId).get();
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void saveContentReportSummary() {
        // 1. post, reporter = user2로 summarySaveDto 생성 후 호출
        callAndAssertSaveContentReportSummary(post);

        // 2. comment, reporter = user2로 summarySaveDto 생성 후 호출
        callAndAssertSaveContentReportSummary(comment);

        // 3. user = user1, reporter = user2로 summarySaveDto 생성 후 호출
        callAndAssertSaveContentReportSummary(users[0]);
    }

    private <T> void callAndAssertSaveContentReportSummary(T contents) {
        ContentReportSummarySaveDto summarySaveDto = createSummarySaveDto(contents);
        Long id = reportSummaryService.saveContentReportSummary(summarySaveDto);

        assertSaveContentReportSummary(id, contents);
    }

    private <T> ContentReportSummarySaveDto createSummarySaveDto(T contents) {
        Posts post = null;
        Comments comment = null;
        User user = null;
        Types type;
        Reasons reason = reasonsRepository.findById(reasonsRepository.count()).get();

        if (contents instanceof Posts) {
            post = (Posts) contents;
            type = typesRepository.findByDetail(ContentType.POSTS.getDetail()).get();
        } else if (contents instanceof Comments) {
            comment = (Comments) contents;
            type = typesRepository.findByDetail(ContentType.COMMENTS.getDetail()).get();
        } else {
            user = (User) contents;
            type = typesRepository.findByDetail(ContentType.USERS.getDetail()).get();
        }

        return ContentReportSummarySaveDto.builder()
                .posts(post).comments(comment).user(user)
                .types(type).reportedDate(LocalDateTime.now()).reporter(users[1])
                .reasons(reason).otherReasons("기타 사유")
                .build();
    }

    private <T> void assertSaveContentReportSummary(Long id, T contents) {
        Optional<ContentReportSummary> result = contentReportSummaryRepository.findById(id);
        assertThat(result).isPresent();

        ContentReportSummary reportSummary = result.get();
        assertThat(reportSummary.getReporter().getId()).isEqualTo(users[1].getId());
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
    public void updateContentReportSummary() {
        saveReportSummaries();

        // 1. contents = post
        callAndAssertUpdateContentReportSummary(post);

        // 2. contents = comment
        callAndAssertUpdateContentReportSummary(comment);

        // 3. contents = user1
        callAndAssertUpdateContentReportSummary(users[0]);
    }

    private void saveReportSummaries() {
        testDB.savePostReportSummary(post, users[1]);
        testDB.saveCommentReportSummary(comment, users[1]);
        this.userReportSummaryId = testDB.saveUserReportSummary(users[0], users[1]);
        assertThat(contentReportSummaryRepository.count()).isEqualTo(userReportSummaryId);
    }

    private <T> void callAndAssertUpdateContentReportSummary(T contents) {
        ContentReportSummary reportSummary = contentReportSummaryRepository.findByContents(contents);

        ContentReportSummarySaveDto summarySaveDto = createSummarySaveDto(contents);
        Long id = reportSummaryService.updateContentReportSummary(summarySaveDto, contents);

        assertUpdateContentReportSummary(id, reportSummary);
    }

    private void assertUpdateContentReportSummary(Long id, ContentReportSummary reportSummary) {
        Optional<ContentReportSummary> result = contentReportSummaryRepository.findById(id);
        assertThat(result).isPresent();

        ContentReportSummary updatedReportSummary = result.get();
        assertThat(updatedReportSummary.getReportedDate().isAfter(reportSummary.getReportedDate())).isTrue();
    }

    @Test
    public void saveOrUpdateContentReportSummary() {
        // 1. 신고 요약 저장 전 => 저장
        callAndAssertSaveOrUpdateContentReportSummary(post);
        callAndAssertSaveOrUpdateContentReportSummary(comment);
        callAndAssertSaveOrUpdateContentReportSummary(users[0]);

        // 2. 신고 요약 저장 후 => 업데이트
        callAndAssertSaveOrUpdateContentReportSummary(post);
        callAndAssertSaveOrUpdateContentReportSummary(comment);
        callAndAssertSaveOrUpdateContentReportSummary(users[0]);
    }

    private <T> void callAndAssertSaveOrUpdateContentReportSummary(T contents) {
        ContentReportSummarySaveDto summarySaveDto = createSummarySaveDto(contents);
        Long id = reportSummaryService.saveOrUpdateContentReportSummary(summarySaveDto);

        assertSaveOrUpdateContentReportSummary(id, contents);
    }

    private <T> void assertSaveOrUpdateContentReportSummary(Long id, T contents) {
        Optional<ContentReportSummary> result = contentReportSummaryRepository.findById(id);
        assertThat(result).isPresent();

        ContentReportSummary reportSummary = result.get();
        assertContentId(reportSummary, contents);
    }

    @Test
    public void findAllDesc() {
        saveReportSummaries();

        callAndAssertFindAllDesc();
    }

    private void callAndAssertFindAllDesc() {
        List<ContentReportSummaryDto> summaryDtos = reportSummaryService.findAllDesc();
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
        List<ContentReportSummaryDto> summaryDtos = reportSummaryService.findByTypes(contentType);

        assertFindByTypes(summaryDtos, expectedType);
    }

    private void assertFindByTypes(List<ContentReportSummaryDto> summaryDtos, Types expectedType) {
        assertThat(summaryDtos.size()).isOne();

        ContentReportSummaryDto summaryDto = summaryDtos.get(0);
        assertThat(summaryDto.getType()).isEqualTo(expectedType.getDetail());
    }

    @Test
    public void deleteSelectedReportSummary() {
        saveReportSummaries();

        callAndAssertDeleteSelectedReportSummary();
    }

    private void callAndAssertDeleteSelectedReportSummary() {
        ContentReportSummaryDeleteDto deleteDto = createDeleteDto();
        reportSummaryService.deleteSelectedReportSummary(deleteDto);

        assertThat(contentReportSummaryRepository.count()).isZero();
    }

    private ContentReportSummaryDeleteDto createDeleteDto() {
        return ContentReportSummaryDeleteDto.builder()
                .postsId(List.of(post.getId()))
                .commentsId(List.of(comment.getId()))
                .userId(List.of(users[0].getId())).build();
    }
}
