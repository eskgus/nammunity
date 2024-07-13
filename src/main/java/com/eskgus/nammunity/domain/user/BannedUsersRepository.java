package com.eskgus.nammunity.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BannedUsersRepository extends JpaRepository<BannedUsers, Long> {
    Optional<BannedUsers> findByUser(User user);
}
