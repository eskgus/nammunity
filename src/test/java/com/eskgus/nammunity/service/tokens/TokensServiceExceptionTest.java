package com.eskgus.nammunity.service.tokens;

import com.eskgus.nammunity.domain.tokens.TokensRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.TOKEN_NOT_FOUND;
import static com.eskgus.nammunity.domain.enums.Fields.TOKEN;
import static com.eskgus.nammunity.util.ServiceTestUtil.assertIllegalArgumentException;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TokensServiceExceptionTest {
    @Mock
    private TokensRepository tokensRepository;

    @InjectMocks
    private TokensService tokensService;

    @Test
    public void findTokensByTokenWithNonExistentToken() {
        // given
        String token = TOKEN.getKey();

        // when/then
        assertIllegalArgumentException(() -> tokensService.findByToken(token), TOKEN_NOT_FOUND);

        verify(tokensRepository).findByToken(eq(token));
    }
}
