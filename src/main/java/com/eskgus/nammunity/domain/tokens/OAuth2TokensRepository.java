package com.eskgus.nammunity.domain.tokens;

import com.eskgus.nammunity.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuth2TokensRepository extends JpaRepository<OAuth2Tokens, Long> {
    Optional<OAuth2Tokens> findByUser(User user);
}
