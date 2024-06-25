package com.eskgus.nammunity.web.reports;

import com.eskgus.nammunity.config.WebConfig;
import com.eskgus.nammunity.config.interceptor.CommentsAuthInterceptor;
import com.eskgus.nammunity.config.interceptor.PostsAuthInterceptor;
import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.handler.CustomControllerAdvice;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.service.reports.ReportSummaryService;
import com.eskgus.nammunity.service.reports.ReportsService;
import com.eskgus.nammunity.service.user.BannedUsersService;
import com.eskgus.nammunity.web.controller.api.reports.ReportsApiController;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDeleteDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportsSaveDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.OTHER_REASONS;
import static com.eskgus.nammunity.domain.enums.Fields.REASONS_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ReportsApiController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CustomControllerAdvice.class))
@Import(MockMvcTestHelper.class)
@MockBeans({ @MockBean(WebConfig.class), @MockBean(PostsAuthInterceptor.class), @MockBean(CommentsAuthInterceptor.class) })
public class ReportsApiControllerExceptionUnitTest {
    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @MockBean
    private ReportsService reportsService;

    @MockBean
    private BannedUsersService bannedUsersService;

    @MockBean
    private ReportSummaryService reportSummaryService;

    private static final Long ID = 1L;

    private static final String REQUEST_MAPPING = "/api/reports";

    @Test
    @WithMockUser
    public void saveContentReportsWithEmptyReasonsId() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(null, null);

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(REASONS_ID, null, EMPTY_REASON_ID);
        testSaveContentReportsException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void saveContentReportsWithInvalidOtherReasonsLength() throws Exception {
        // given
        String otherReasons = "10 letters".repeat(50) + "!";
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(ID, otherReasons);

        // when/then
        ResultMatcher[] resultMatchers = createResultMatchers(OTHER_REASONS, otherReasons, INVALID_OTHER_REASON);
        testSaveContentReportsException(requestDto, never(), resultMatchers);
    }

    @Test
    @WithMockUser
    public void saveContentReportsThrowsIllegalArgumentException() throws Exception {
        // given
        ContentReportsSaveDto requestDto = createContentReportsSaveDto(ID, null);

        when(reportsService.saveContentReports(any(ContentReportsSaveDto.class), any(Principal.class)))
                .thenThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()));

        // when/then
        ResultMatcher resultMatcher = createResultMatcher();
        testSaveContentReportsException(requestDto, times(1), resultMatcher);
    }

    @Test
    @WithMockUser
    public void deleteSelectedContentReportsThrowsIllegalArgumentException() throws Exception {
        // given
        ContentReportSummaryDeleteDto requestDto = ContentReportSummaryDeleteDto.builder()
                .postsId(Arrays.asList(ID, ID + 1, ID + 2))
                .commentsId(Collections.singletonList(ID))
                .userId(Collections.emptyList()).build();

        doThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()))
                .when(reportSummaryService).deleteSelectedReportSummary(any(ContentReportSummaryDeleteDto.class));

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/content/selected-delete");
        ResultMatcher resultMatcher = createResultMatcher();
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatcher);

        verify(reportSummaryService).deleteSelectedReportSummary(any(ContentReportSummaryDeleteDto.class));
    }

    @Test
    @WithMockUser
    public void banUserThrowsIllegalArgumentException() throws Exception {
        // given
        when(bannedUsersService.banUser(anyLong()))
                .thenThrow(new IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_TEST.getMessage()));

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/process");
        ResultMatcher resultMatcher = createResultMatcher();
        performAndExpectBadRequest(requestBuilder, ID, resultMatcher);

        verify(bannedUsersService).banUser(eq(ID));
    }

    private ContentReportsSaveDto createContentReportsSaveDto(Long reasonsId, String otherReasons) {
        ContentReportsSaveDto requestDto = new ContentReportsSaveDto();
        requestDto.setReasonsId(reasonsId);
        requestDto.setOtherReasons(otherReasons);

        return requestDto;
    }

    private ResultMatcher[] createResultMatchers(Fields field, String rejectedValue, ExceptionMessages exceptionMessage) {
        return mockMvcTestHelper.createResultMatchers(field, rejectedValue, exceptionMessage);
    }

    private ResultMatcher createResultMatcher() {
        return mockMvcTestHelper.createResultMatcher(ILLEGAL_ARGUMENT_EXCEPTION_TEST);
    }

    private void testSaveContentReportsException(ContentReportsSaveDto requestDto, VerificationMode mode,
                                                 ResultMatcher... resultMatchers) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/content");
        performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);

        verify(reportsService, mode).saveContentReports(any(ContentReportsSaveDto.class), any(Principal.class));
    }

    private <T> void performAndExpectBadRequest(MockHttpServletRequestBuilder requestBuilder, T requestDto,
                                                ResultMatcher... resultMatchers) throws Exception {
        mockMvcTestHelper.performAndExpectBadRequest(requestBuilder, requestDto, resultMatchers);
    }
}
