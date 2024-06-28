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

    public ResultMatcher[] createResultMatchers(Fields field, String rejectedValue, ExceptionMessages exceptionMessage) {
        return new ResultMatcher[]{
                jsonPath("$[0].field").value(field.getKey()),
                jsonPath("$[0].rejectedValue").value(rejectedValue),
                jsonPath("$[0].defaultMessage").value(exceptionMessage.getMessage())
        };
    }

    public <Dto> void performAndExpectOk(MockHttpServletRequestBuilder requestBuilder, Dto requestDto) throws Exception {
        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    public <Dto> void performAndExpectBadRequest(MockHttpServletRequestBuilder requestBuilder, Dto requestDto,
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

    public <Dto> void performAndExpectUnauthorized(MockHttpServletRequestBuilder requestBuilder, Dto requestDto) throws Exception {
        ResultMatcher resultMatcher = createResultMatcher(ExceptionMessages.UNAUTHORIZED);

        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(resultMatcher);
    }

    public <Dto> void performAndExpectForbidden(MockHttpServletRequestBuilder requestBuilder, Dto requestDto) throws Exception {
        ResultMatcher resultMatcher = createResultMatcher(ExceptionMessages.FORBIDDEN);

        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(resultMatcher);
    }

    public <Value> void performAndExpectOkWithParam(MockHttpServletRequestBuilder requestBuilder,
                                                    Fields field, Value value) throws Exception {
        mockMvc.perform(requestBuilder
                        .param(field.getKey(), value.toString())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    public <Value> void performAndExpectBadRequestWithParam(MockHttpServletRequestBuilder requestBuilder,
                                                            Fields field, Value value, ResultMatcher... resultMatchers) throws Exception {
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

    public <Value> void performAndExpectUnauthorizedWithParam(MockHttpServletRequestBuilder requestBuilder,
                                                              Fields field, Value value) throws Exception {
        ResultMatcher resultMatcher = createResultMatcher(ExceptionMessages.UNAUTHORIZED);

        mockMvc.perform(requestBuilder
                        .param(field.getKey(), value.toString())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(resultMatcher);
    }

    public void performAndExpectFound(MockHttpServletRequestBuilder requestBuilder,
                                      String token, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(requestBuilder
                        .param("token", token)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/users/confirm-email"))
                .andExpect(resultMatcher);
    }

    public void performAndExpectOkWithReferer(MockHttpServletRequestBuilder requestBuilder, String referer,
                                              ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(requestBuilder
                        .header("referer", referer)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(resultMatcher);
    }

    public void performAndExpectBadRequestWithReferer(MockHttpServletRequestBuilder requestBuilder,
                                                      String referer, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(requestBuilder
                        .header("referer", referer)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(resultMatcher);
    }

    public void performAndExpectOkWithCookie(MockHttpServletRequestBuilder requestBuilder,
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

    public void performAndExpectBadRequestWithCookie(MockHttpServletRequestBuilder requestBuilder,
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

    public void performAndExpectInternalServerError(MockHttpServletRequestBuilder requestBuilder,
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
