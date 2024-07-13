package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.reports.ContentReportSummaryRepository;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDeleteDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.ContentType.*;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportSummaryServiceExceptionTest {
    @Mock
    private ContentReportSummaryRepository contentReportSummaryRepository;

    @Mock
    private TypesService typesService;

    @Mock
    private PostsService postsService;

    @Mock
    private CommentsService commentsService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ReportSummaryService reportSummaryService;

    @Test
    public void findSummaryByTypesWithNonExistentType() {
        // given
        ContentType contentType = POSTS;
        int page = 1;

        ExceptionMessages exceptionMessage = TYPE_NOT_FOUND;
        throwIllegalArgumentException(typesService::findByContentType, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> reportSummaryService.findByTypes(contentType, page), exceptionMessage);

        verify(typesService).findByContentType(contentType);
        verify(contentReportSummaryRepository, never()).findByTypes(any(Types.class), any(Pageable.class));
    }

    @Test
    public void findSummaryByUserWithNonExistentUserReportSummary() {
        // given
        User user = mock(User.class);

        // when/then
        assertIllegalArgumentException(() -> reportSummaryService.findByUser(user), USER_REPORT_SUMMARY_NOT_FOUND);

        verify(contentReportSummaryRepository).findByUser(eq(user));
    }

    @Test
    public void deleteSelectedSummariesWithEmptyContentIds() {
        // given
        ContentReportSummaryDeleteDto requestDto = createSummaryDeleteDto(null);

        // when/then
        testDeleteSelectedSummariesException(null, requestDto, EMPTY_CONTENT_IDS);
    }

    @Test
    public void deleteSelectedSummariesWithNonExistentPost() {
        testDeleteSelectedSummariesNotFoundContentException(POSTS, postsService::findById, POST_NOT_FOUND);
    }

    @Test
    public void deleteSelectedSummariesWithNonExistentComment() {
        testDeleteSelectedSummariesNotFoundContentException(COMMENTS, commentsService::findById, COMMENT_NOT_FOUND);
    }

    @Test
    public void deleteSelectedSummariesWithNonExistentUser() {
        testDeleteSelectedSummariesNotFoundContentException(USERS, userService::findById, USER_NOT_FOUND);
    }

    private <Entity> void testDeleteSelectedSummariesNotFoundContentException(ContentType contentType,
                                                                              Function<Long, Entity> finder,
                                                                              ExceptionMessages exceptionMessage) {
        // given
        ContentReportSummaryDeleteDto requestDto = createSummaryDeleteDto(contentType);

        throwIllegalArgumentException(finder, exceptionMessage);

        // when/then
        testDeleteSelectedSummariesException(contentType, requestDto, exceptionMessage);
    }

    private void testDeleteSelectedSummariesException(ContentType contentType, ContentReportSummaryDeleteDto requestDto,
                                                      ExceptionMessages exceptionMessage) {
        List<VerificationMode> modes = ServiceTestUtil.setModes(contentType);

        assertIllegalArgumentException(
                () -> reportSummaryService.deleteSelectedReportSummaries(requestDto), exceptionMessage);

        verify(postsService, modes.get(0)).findById(anyLong());
        verify(commentsService, modes.get(1)).findById(anyLong());
        verify(userService, modes.get(2)).findById(anyLong());
        verify(contentReportSummaryRepository, never()).deleteByElement(any());
    }

    private ContentReportSummaryDeleteDto createSummaryDeleteDto(ContentType contentType) {
        List<Long> postIds = Collections.emptyList();
        List<Long> commentIds = Collections.emptyList();
        List<Long> userIds = Collections.emptyList();

        Long id = 1L;

        if (contentType != null) {
            switch (contentType) {
                case POSTS -> postIds = Collections.singletonList(id);
                case COMMENTS -> commentIds = Collections.singletonList(id);
                case USERS -> userIds = Collections.singletonList(id);
            }
        }

        return ContentReportSummaryDeleteDto.builder()
                .postsId(postIds).commentsId(commentIds).userId(userIds).build();
    }

    private <Entity, ParamType> void throwIllegalArgumentException(Function<ParamType, Entity> finder,
                                                                   ExceptionMessages exceptionMessage) {
        ServiceTestUtil.throwIllegalArgumentException(finder, exceptionMessage);
    }

    private void assertIllegalArgumentException(Executable executable, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.assertIllegalArgumentException(executable, exceptionMessage);
    }
}
