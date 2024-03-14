package com.eskgus.nammunity.domain.user;

import java.time.LocalDateTime;

public interface CustomBannedUsersRepository {
    void updateExpiredDate(Long id, LocalDateTime expiredDate);
}
