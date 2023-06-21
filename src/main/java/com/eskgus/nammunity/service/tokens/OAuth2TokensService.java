package com.eskgus.nammunity.service.tokens;

import com.eskgus.nammunity.domain.tokens.OAuth2Tokens;
import com.eskgus.nammunity.domain.tokens.OAuth2TokensRepository;
import com.eskgus.nammunity.web.dto.tokens.OAuth2TokensDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OAuth2TokensService {
    private final OAuth2TokensRepository oAuth2TokensRepository;

    @Transactional
    public void save(OAuth2TokensDto oAuth2TokensDto) {
        oAuth2TokensRepository.save(oAuth2TokensDto.toEntity());
    }

    @Transactional
    public void update(OAuth2TokensDto oAuth2TokensDto) {
        OAuth2Tokens oAuth2Tokens = oAuth2TokensRepository.findByUser(oAuth2TokensDto.getUser()).get();
        oAuth2Tokens.update(oAuth2TokensDto.getRefreshToken(), oAuth2TokensDto.getExpiredAt());
    }
}
