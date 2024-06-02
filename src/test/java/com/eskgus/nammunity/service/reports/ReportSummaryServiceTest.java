package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.converter.ContentReportSummaryConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.CommentsRepository;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.PostsRepository;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.ContentsPageDtoTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDeleteDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummarySaveDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ReportSummaryServiceTest {
    @Autowired
    private TestDataHelper testDataHelper;

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

    private User[] users;
    private Posts post;
    private Comments comment;
    private final int page = 1;

    @BeforeEach
    public void setUp() {
        // 1. user1 회원가입 + user2 회원가입
        Long user1Id = testDataHelper.signUp(1L, Role.USER);
        User user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDataHelper.signUp(2L, Role.USER);
        User user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        this.users = new User[]{ user1, user2 };

        // 2. user1이 게시글 작성
        Long postId = testDataHelper.savePosts(user1);
        this.post = assertOptionalAndGetEntity(postsRepository::findById, postId);

        // 3. user1이 댓글 작성
        Long commentId = testDataHelper.saveComments(postId, user1);
        this.comment = assertOptionalAndGetEntity(commentsRepository::findById, commentId);
    }

    private <T,U> T assertOptionalAndGetEntity(Function<U, Optional<T>> finder, U content) {
        return testDataHelper.assertOptionalAndGetEntity(finder, content);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
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
        Reasons reason = assertOptionalAndGetEntity(reasonsRepository::findById, reasonsRepository.count());

        if (contents instanceof Posts) {
            post = (Posts) contents;
            type = assertOptionalAndGetEntity(typesRepository::findByDetail, ContentType.POSTS.getDetailInKor());
        } else if (contents instanceof Comments) {
            comment = (Comments) contents;
            type = assertOptionalAndGetEntity(typesRepository::findByDetail, ContentType.COMMENTS.getDetailInKor());
        } else {
            user = (User) contents;
            type = assertOptionalAndGetEntity(typesRepository::findByDetail, ContentType.USERS.getDetailInKor());
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
        testDataHelper.savePostReportSummary(post, users[1]);
        testDataHelper.saveCommentReportSummary(comment, users[1]);
        Long userReportSummaryId = testDataHelper.saveUserReportSummary(users[0], users[1]);
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
        ContentsPageDto<ContentReportSummaryDto> actualResult = reportSummaryService.findAllDesc(page);
        Page<ContentReportSummaryDto> expectedContents = createExpectedContents();

        ContentsPageDtoTestHelper<ContentReportSummaryDto, ContentReportSummary> findHelper
                = ContentsPageDtoTestHelper.<ContentReportSummaryDto, ContentReportSummary>builder()
                .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(new ContentReportSummaryConverterForTest()).build();
        findHelper.createExpectedResultAndAssertContentsPage();
    }

    private Page<ContentReportSummaryDto> createExpectedContents() {
        Pageable pageable = createPageable(page, 20);
        return contentReportSummaryRepository.findAllDesc(pageable);
    }

    @Test
    public void findByTypes() {
        saveReportSummaries();

        callAndAssertFindByTypes(ContentType.POSTS);
        callAndAssertFindByTypes(ContentType.COMMENTS);
        callAndAssertFindByTypes(ContentType.USERS);
    }

    private void callAndAssertFindByTypes(ContentType contentType) {
        ContentsPageDto<ContentReportSummaryDto> actualResult = reportSummaryService.findByTypes(contentType, page);
        Page<ContentReportSummaryDto> expectedContents = createExpectedContentsByTypes(contentType);

        ContentsPageDtoTestHelper<ContentReportSummaryDto, ContentReportSummary> findHelper
                = ContentsPageDtoTestHelper.<ContentReportSummaryDto, ContentReportSummary>builder()
                .actualResult(actualResult).expectedContents(expectedContents)
                .entityConverter(new ContentReportSummaryConverterForTest()).build();
        findHelper.createExpectedResultAndAssertContentsPage();
    }

    private Page<ContentReportSummaryDto> createExpectedContentsByTypes(ContentType contentType) {
        Pageable pageable = createPageable(page, 20);
        Types type = assertOptionalAndGetEntity(typesRepository::findByDetail, contentType.getDetailInKor());
        return contentReportSummaryRepository.findByTypes(type, pageable);
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
