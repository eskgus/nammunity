package com.eskgus.nammunity.service.tokens;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.tokens.TokensRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class TokensService {
    private final TokensRepository tokensRepository;

    public Optional<Tokens> findByToken(String token) {
        return tokensRepository.findByToken(token);
    }

    @Transactional
    public void save(Tokens tokens) {
        tokensRepository.save(tokens);
    }
}
