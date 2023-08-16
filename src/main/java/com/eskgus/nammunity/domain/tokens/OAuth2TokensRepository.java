package com.eskgus.nammunity.domain.tokens;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2TokensRepository extends JpaRepository<OAuth2Tokens, Long> {
}
