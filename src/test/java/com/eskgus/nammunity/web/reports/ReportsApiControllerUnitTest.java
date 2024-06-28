package com.eskgus.nammunity.web.reports;

import com.eskgus.nammunity.config.WebConfig;
import com.eskgus.nammunity.config.interceptor.CommentsAuthInterceptor;
import com.eskgus.nammunity.config.interceptor.PostsAuthInterceptor;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ReportsApiController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CustomControllerAdvice.class))
@Import(MockMvcTestHelper.class)
@MockBeans({ @MockBean(WebConfig.class), @MockBean(PostsAuthInterceptor.class), @MockBean(CommentsAuthInterceptor.class) })
public class ReportsApiControllerUnitTest {
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
    public void saveContentReports() throws Exception {
        // given
        ContentReportsSaveDto requestDto = new ContentReportsSaveDto();
        requestDto.setReasonsId(ID);

        when(reportsService.saveContentReports(any(ContentReportsSaveDto.class), any(Principal.class))).thenReturn(ID);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/content");
        performAndExpectOk(requestBuilder, requestDto);

        verify(reportsService).saveContentReports(any(ContentReportsSaveDto.class), any(Principal.class));
    }

    @Test
    @WithMockUser
    public void deleteSelectedReportSummaries() throws Exception {
        // given
        ContentReportSummaryDeleteDto requestDto = ContentReportSummaryDeleteDto.builder()
                .postsId(Arrays.asList(ID, ID + 1, ID + 2))
                .commentsId(Collections.singletonList(ID))
                .userId(Collections.emptyList()).build();

        doNothing().when(reportSummaryService).deleteSelectedReportSummaries(any(ContentReportSummaryDeleteDto.class));

        // when/then
        MockHttpServletRequestBuilder requestBuilder = delete(REQUEST_MAPPING + "/content/selected-delete");
        performAndExpectOk(requestBuilder, requestDto);

        verify(reportSummaryService).deleteSelectedReportSummaries(any(ContentReportSummaryDeleteDto.class));
    }

    @Test
    @WithMockUser
    public void banUser() throws Exception {
        // given
        when(bannedUsersService.banUser(anyLong())).thenReturn(ID);

        // when/then
        MockHttpServletRequestBuilder requestBuilder = post(REQUEST_MAPPING + "/process");
        performAndExpectOk(requestBuilder, ID);

        verify(bannedUsersService).banUser(eq(ID));
    }

    private <Dto> void performAndExpectOk(MockHttpServletRequestBuilder requestBuilder, Dto requestDto) throws Exception {
        mockMvcTestHelper.performAndExpectOk(requestBuilder, requestDto);
    }
}
