package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.enums.Fields;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Component
public class MockMvcTestHelper {
    private final MockMvc mockMvc;

    @Autowired
    public MockMvcTestHelper(WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    public ResultMatcher createResultMatcher(String expectedContent) {
        return content().string(expectedContent);
    }

    public ResultMatcher createResultMatcher(ExceptionMessages exceptionMessage) {
        return content().string(exceptionMessage.getMessage());
    }

    public ResultMatcher[] createResultMatchers(String expectedField, String expectedDefaultMessage,
                                                String expectedRejectedValue) {
        return new ResultMatcher[]{
                jsonPath("$[0].field").value(expectedField),
                jsonPath("$[0].defaultMessage").value(expectedDefaultMessage),
                jsonPath("$[0].rejectedValue").value(expectedRejectedValue)
        };
    }

    public ResultMatcher[] createResultMatchers(Fields field, String rejectedValue, ExceptionMessages exceptionMessage) {
        return new ResultMatcher[]{
                jsonPath("$[0].field").value(field.getKey()),
                jsonPath("$[0].rejectedValue").value(rejectedValue),
                jsonPath("$[0].defaultMessage").value(exceptionMessage.getMessage())
        };
    }

    public <T> void requestAndAssertStatusIsOk(MockHttpServletRequestBuilder requestBuilder, T requestDto) throws Exception {
        mockMvc.perform(requestBuilder
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(requestDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    public <T> void performAndExpectOk(MockHttpServletRequestBuilder requestBuilder, T requestDto) throws Exception {
        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    public <T> void requestAndAssertStatusIsOkWithParam(MockHttpServletRequestBuilder requestBuilder,
                                                        String name, T value) throws Exception {
        mockMvc.perform(requestBuilder
                        .param(name, value.toString())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    public <T> void performAndExpectOkWithParam(MockHttpServletRequestBuilder requestBuilder,
                                                Fields field, T value) throws Exception {
        mockMvc.perform(requestBuilder
                        .param(field.getKey(), value.toString())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    public void requestAndAssertStatusIsOkWithReferer(MockHttpServletRequestBuilder requestBuilder,
                                                      String referer, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(requestBuilder
                    .header("referer", referer)
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(resultMatcher);
    }

    public void requestAndAssertStatusIsOkWithCookie(MockHttpServletRequestBuilder requestBuilder,
                                                     Cookie cookie, ResultMatcher resultMatcher) throws Exception {
        if (cookie != null) {
            requestBuilder.cookie(cookie);
        }

        mockMvc.perform(requestBuilder
                    .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(resultMatcher);
    }

    public void requestAndAssertStatusIsFound(MockHttpServletRequestBuilder requestBuilder,
                                              String token, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(requestBuilder
                        .param("token", token)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/users/confirm-email"))
                .andExpect(resultMatcher);
    }

    public <T> void requestAndAssertStatusIsBadRequest(MockHttpServletRequestBuilder requestBuilder, T requestDto,
                                                       ResultMatcher... resultMatchers) throws Exception {
        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    for (ResultMatcher resultMatcher : resultMatchers) {
                        resultMatcher.match(result);
                    }
                });
    }

    public <T> void performAndExpectBadRequest(MockHttpServletRequestBuilder requestBuilder, T requestDto,
                                               ResultMatcher... resultMatchers) throws Exception {
        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    for (ResultMatcher resultMatcher : resultMatchers) {
                        resultMatcher.match(result);
                    }
                });
    }

    public <T> void requestAndAssertStatusIsBadRequestWithParam(MockHttpServletRequestBuilder requestBuilder,
                                                            String name, T value, ResultMatcher... resultMatchers) throws Exception {
        mockMvc.perform(requestBuilder
                        .param(name, value.toString())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    for (ResultMatcher resultMatcher : resultMatchers) {
                        resultMatcher.match(result);
                    }
                });
    }

    public <T> void performAndExpectBadRequestWithParam(MockHttpServletRequestBuilder requestBuilder,
                                                        Fields field, T value, ResultMatcher... resultMatchers) throws Exception {
        mockMvc.perform(requestBuilder
                        .param(field.getKey(), value.toString())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    for (ResultMatcher resultMatcher : resultMatchers) {
                        resultMatcher.match(result);
                    }
                });
    }

    public void requestAndAssertStatusIsBadRequestWithReferer(MockHttpServletRequestBuilder requestBuilder,
                                                              String referer,
                                                              ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(requestBuilder
                        .header("referer", referer)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(resultMatcher);
    }

    public void requestAndAssertStatusIsBadRequestWithCookie(MockHttpServletRequestBuilder requestBuilder,
                                                             Cookie cookie, ResultMatcher resultMatcher) throws Exception {
        if (cookie != null) {
            requestBuilder.cookie(cookie);
        }

        mockMvc.perform(requestBuilder
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(resultMatcher);
    }

    public <T> void performAndExpectUnauthorized(MockHttpServletRequestBuilder requestBuilder, T requestDto) throws Exception {
        ResultMatcher resultMatcher = createResultMatcher(ExceptionMessages.UNAUTHORIZED);

        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(resultMatcher);
    }

    public <T> void performAndExpectUnauthorizedWithParam(MockHttpServletRequestBuilder requestBuilder,
                                                          Fields field, T value) throws Exception {
        ResultMatcher resultMatcher = createResultMatcher(ExceptionMessages.UNAUTHORIZED);

        mockMvc.perform(requestBuilder
                        .param(field.getKey(), value.toString())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(resultMatcher);
    }

    public <T> void performAndExpectForbidden(MockHttpServletRequestBuilder requestBuilder, T requestDto) throws Exception {
        ResultMatcher resultMatcher = createResultMatcher(ExceptionMessages.FORBIDDEN);

        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(resultMatcher);
    }

    public void requestAndAssertStatusIsInternalServerError(MockHttpServletRequestBuilder requestBuilder,
                                                            Cookie cookie, ResultMatcher resultMatcher) throws Exception {
        if (cookie != null) {
            requestBuilder.cookie(cookie);
        }

        mockMvc.perform(requestBuilder
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(resultMatcher);
    }
}
