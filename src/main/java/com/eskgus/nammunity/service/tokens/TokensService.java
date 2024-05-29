package com.eskgus.nammunity.service.tokens;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.tokens.TokensRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TokensService {
    private final TokensRepository tokensRepository;

    @Transactional(readOnly = true)
    public Tokens findByToken(String token) {
        return tokensRepository.findByToken(token).orElseThrow(() -> new
                IllegalArgumentException("인증 링크가 존재하지 않습니다."));
    }

    @Transactional
    public void save(Tokens tokens) {
        tokensRepository.save(tokens);
    }
}
