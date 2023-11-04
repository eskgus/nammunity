package com.eskgus.nammunity.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BannedUsersRepository extends JpaRepository<BannedUsers, Long> {
    Optional<BannedUsers> findByUser(User user);

    @Modifying
    @Query("UPDATE BannedUsers bu SET bu.expiredDate = :expiredDate WHERE bu.user = :user")
    void updateExpiredDate(User user, LocalDateTime expiredDate);
}
