package com.eskgus.nammunity.web.dto.tokens;

import com.eskgus.nammunity.domain.tokens.OAuth2Tokens;
import com.eskgus.nammunity.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class OAuth2TokensDto {
    private String refreshToken;
    private LocalDateTime expiredAt;
    private User user;

    @Builder
    public OAuth2TokensDto(String refreshToken, LocalDateTime expiredAt, User user) {
        this.refreshToken = refreshToken;
        this.expiredAt = expiredAt;
        this.user = user;
    }

    public OAuth2Tokens toEntity() {
        return OAuth2Tokens.builder()
                .refreshToken(refreshToken)
                .expiredAt(expiredAt)
                .user(user).build();
    }
}
