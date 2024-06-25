package com.eskgus.nammunity.service.tokens;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.tokens.TokensRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.TOKEN_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class TokensService {
    private final TokensRepository tokensRepository;

    @Transactional(readOnly = true)
    public Tokens findByToken(String token) {
        return tokensRepository.findByToken(token).orElseThrow(() -> new
                IllegalArgumentException(TOKEN_NOT_FOUND.getMessage()));
    }

    @Transactional
    public void save(Tokens tokens) {
        tokensRepository.save(tokens);
    }
}
