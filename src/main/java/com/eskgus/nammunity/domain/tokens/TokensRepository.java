package com.eskgus.nammunity.domain.tokens;

import com.eskgus.nammunity.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TokensRepository extends JpaRepository<Tokens, Long> {
    Optional<Tokens> findByToken(String token);

    @Modifying
    @Query("UPDATE Tokens t SET t.expiredAt = :now WHERE t.user = :user AND t.expiredAt > :now")
    void updateExpiredAtAllByUser(User user, LocalDateTime now);

    void deleteAllByUser(User user);
    List<Tokens> findByUser(User user);
}
