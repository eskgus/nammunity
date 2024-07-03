package com.eskgus.nammunity.service.tokens;

import com.eskgus.nammunity.domain.enums.Fields;
import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.tokens.TokensRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.util.ServiceTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokensServiceTest {
    @Mock
    private TokensRepository tokensRepository;

    @InjectMocks
    private TokensService tokensService;

    private static final String TOKEN = Fields.TOKEN.getKey();

    @Test
    public void findTokensByToken() {
        // given
        Tokens token = mock(Tokens.class);
        ServiceTestUtil.giveContentFinder(tokensRepository::findByToken, String.class, token);
        when(tokensRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        // when
        Tokens result = tokensService.findByToken(TOKEN);

        // then
        assertEquals(token, result);

        verify(tokensRepository).findByToken(eq(TOKEN));
    }

    @Test
    public void saveTokens() {
        // given
        User user = mock(User.class);
        LocalDateTime now = LocalDateTime.now();

        Tokens requestDto = Tokens.builder()
                .token(TOKEN).user(user).createdAt(now).expiredAt(now).build();

        Tokens token = mock(Tokens.class);
        when(token.getId()).thenReturn(1L);
        when(tokensRepository.save(any(Tokens.class))).thenReturn(token);

        // when
        Long result = tokensService.save(requestDto);

        // then
        assertEquals(token.getId(), result);

        verify(tokensRepository).save(eq(requestDto));
    }
}
