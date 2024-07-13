package com.eskgus.nammunity.domain.tokens;

import com.eskgus.nammunity.config.TestConfig;
import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.user.Role;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.helper.TestDataHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({ TestDataHelper.class, TestConfig.class })
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TokensRepositoryTest {
    @Autowired
    private TestDataHelper testDataHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokensRepository tokensRepository;

    @AfterEach
    public void cleanUp() {
        testDataHelper.cleanUp();
    }

    @Test
    public void findTokensByTokenWithoutTokens() {
        // given
        // when/then
        testFindTokensByToken(Fields.TOKEN.getKey(), Optional.empty());
    }

    @Test
    public void findTokensByTokenWithTokens() {
        // given
        Tokens token = saveToken();

        // when/then
        testFindTokensByToken(token.getToken(), Optional.of(token));
    }

    private void testFindTokensByToken(String tokenValue, Optional<Tokens> token) {
        // when
        Optional<Tokens> result = tokensRepository.findByToken(tokenValue);

        // then
        assertEquals(token, result);
    }

    private Tokens saveToken() {
        User user = saveUser();
        Long tokenId = testDataHelper.saveTokens(user);
        return assertOptionalAndGetEntity(tokensRepository::findById, tokenId);
    }

    private User saveUser() {
        Long userId = testDataHelper.signUp(1L, Role.USER);
        return assertOptionalAndGetEntity(userRepository::findById, userId);
    }

    private <Entity> Entity assertOptionalAndGetEntity(Function<Long, Optional<Entity>> finder, Long contentId) {
        return testDataHelper.assertOptionalAndGetEntity(finder, contentId);
    }
}
