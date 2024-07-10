package com.eskgus.nammunity.service.email.dto;

import com.eskgus.nammunity.domain.user.BannedUsers;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BannedUsersEmailDto {
    private final String username;
    private final String period;
    private final String startedDate;
    private final String expiredDate;
    private final String reason;

    @Builder
    public BannedUsersEmailDto(BannedUsers bannedUser) {
        this.username = bannedUser.getUser().getUsername();
        this.period = DateTimeUtil.convertPeriodToString(bannedUser.getPeriod());
        this.startedDate = formatDateTime(bannedUser.getStartedDate());
        this.expiredDate = formatDateTime(bannedUser.getExpiredDate());
        this.reason = bannedUser.getReason();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return DateTimeUtil.formatDateTime(dateTime);
    }
}
