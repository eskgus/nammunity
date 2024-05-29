package com.eskgus.nammunity.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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

    public ResultMatcher[] createResultMatchers(String expectedField, String expectedDefaultMessage) {
        return new ResultMatcher[]{
                jsonPath("$[0].field").value(expectedField),
                jsonPath("$[0].defaultMessage").value(expectedDefaultMessage)
        };
    }

    public <T> void requestAndAssertStatusIsOk(MockHttpServletRequestBuilder requestBuilder, T requestDto) throws Exception {
        mockMvc.perform(requestBuilder
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    public <T> void requestAndAssertStatusIsOkWithParam(MockHttpServletRequestBuilder requestBuilder,
                                                        String name, T value) throws Exception {
        mockMvc.perform(requestBuilder
                        .param(name, value.toString()))
                .andExpect(status().isOk());
    }

    public void requestAndAssertStatusIsOkWithReferer(MockHttpServletRequestBuilder requestBuilder,
                                                      String referer, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(requestBuilder
                    .header("referer", referer))
                .andExpect(status().isOk())
                .andExpect(resultMatcher);
    }

    public <T> void requestAndAssertStatusIsBadRequest(MockHttpServletRequestBuilder requestBuilder, T requestDto,
                                                       ResultMatcher... resultMatchers) throws Exception {
        mockMvc.perform(requestBuilder
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto)))
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
                        .param(name, value.toString()))
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
                        .header("referer", referer))
                .andExpect(status().isBadRequest())
                .andExpect(resultMatcher);
    }
}
