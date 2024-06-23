package com.eskgus.nammunity.web.user;

import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.helper.MockMvcTestHelper;
import com.eskgus.nammunity.helper.TestDataHelper;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static com.eskgus.nammunity.domain.enums.Fields.EMAIL;
import static com.eskgus.nammunity.domain.enums.Fields.USERNAME;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SignInApiControllerIntegrationTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private MockMvcTestHelper mockMvcTestHelper;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        this.user = testDataHelper.assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void findUsername() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = get("/api/users/sign-in/username");
        performAndExpectOkWithParam(requestBuilder, EMAIL, user.getEmail());
    }

    @Test
    public void findPassword() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = put("/api/users/sign-in/password");
        performAndExpectOkWithParam(requestBuilder, USERNAME, user.getUsername());
    }

    private void performAndExpectOkWithParam(MockHttpServletRequestBuilder requestBuilder, Fields field,
                                             String value) throws Exception {
        mockMvcTestHelper.performAndExpectOkWithParam(requestBuilder, field, value);
    }
}
