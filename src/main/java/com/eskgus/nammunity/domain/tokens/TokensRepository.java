package com.eskgus.nammunity.domain.tokens;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokensRepository extends JpaRepository<Tokens, Long> {
    Optional<Tokens> findByToken(String token);

}
