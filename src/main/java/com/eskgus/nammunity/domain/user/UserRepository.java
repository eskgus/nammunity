package com.eskgus.nammunity.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    @Modifying
    @Query("UPDATE User u SET u.attempt = 0 WHERE u.id = :id")
    void resetAttempt(Long id);

    @Modifying
    @Query("UPDATE User u SET u.social = 'none' WHERE u.username = :username")
    void resetSocial(String username);
}
