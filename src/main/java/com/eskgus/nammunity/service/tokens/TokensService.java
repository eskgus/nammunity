package com.eskgus.nammunity.service.tokens;

import com.eskgus.nammunity.domain.tokens.Tokens;
import com.eskgus.nammunity.domain.tokens.TokensRepository;
import com.eskgus.nammunity.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Transactional
    public void updateConfirmedAt(String token, LocalDateTime confirmedAt) {
        Tokens tokens = tokensRepository.findByToken(token).get();
        tokens.update(confirmedAt);
    }

    @Transactional
    public void updateExpiredAtAllByUser(User user, LocalDateTime now) {
        tokensRepository.updateExpiredAtAllByUser(user, now);
    }

    @Transactional
    public void deleteAllByUser(User user) {
        tokensRepository.deleteAllByUser(user);
    }
}
