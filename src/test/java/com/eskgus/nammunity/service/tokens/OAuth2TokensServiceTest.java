package com.eskgus.nammunity.service.tokens;

import com.eskgus.nammunity.domain.tokens.OAuth2Tokens;
import com.eskgus.nammunity.domain.tokens.OAuth2TokensRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.tokens.OAuth2TokensDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static com.eskgus.nammunity.domain.enums.Fields.REFRESH_TOKEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OAuth2TokensServiceTest {
    @Mock
    private OAuth2TokensRepository oAuth2TokensRepository;

    @InjectMocks
    private OAuth2TokensService oAuth2TokensService;

    @Test
    public void saveOAuth2Tokens() {
        // given
        OAuth2TokensDto requestDto = createOAuth2TokensDto();

        OAuth2Tokens oAuth2Token = giveOAuth2Tokens();
        when(oAuth2TokensRepository.save(any(OAuth2Tokens.class))).thenReturn(oAuth2Token);

        // when
        Long result = oAuth2TokensService.save(requestDto);

        // then
        assertEquals(oAuth2Token.getId(), result);

        verify(oAuth2TokensRepository).save(any(OAuth2Tokens.class));
    }

    @Test
    public void updateOAuth2Tokens() {
        // given
        OAuth2TokensDto requestDto = createOAuth2TokensDto();

        User user = requestDto.getUser();
        OAuth2Tokens oAuth2Tokens = giveOAuth2Tokens();
        when(user.getOAuth2Tokens()).thenReturn(oAuth2Tokens);

        // when
        Long result = oAuth2TokensService.update(requestDto);

        // then
        assertEquals(oAuth2Tokens.getId(), result);

        verify(oAuth2Tokens).update(eq(requestDto.getRefreshToken()), eq(requestDto.getExpiredAt()));
    }

    private OAuth2TokensDto createOAuth2TokensDto() {
        String refreshToken = REFRESH_TOKEN.getKey();
        LocalDateTime expiredAt = LocalDateTime.now();
        User user = mock(User.class);

        return OAuth2TokensDto.builder().refreshToken(refreshToken).expiredAt(expiredAt).user(user).build();
    }

    private OAuth2Tokens giveOAuth2Tokens() {
        OAuth2Tokens oAuth2Tokens = mock(OAuth2Tokens.class);
        when(oAuth2Tokens.getId()).thenReturn(1L);

        return oAuth2Tokens;
    }
}
