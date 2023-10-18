package com.eskgus.nammunity.service.email.dto;

import com.eskgus.nammunity.domain.user.BannedUsers;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BannedUsersEmailDto {
    private String username;
    private String period;
    private String startedDate;
    private String expiredDate;
    private String reason;

    @Builder
    public BannedUsersEmailDto(BannedUsers bannedUser, String reason) {
        this.username = bannedUser.getUser().getUsername();
        this.period = DateTimeUtil.convertPeriodToString(bannedUser.getPeriod());
        this.startedDate = DateTimeUtil.formatDateTime(bannedUser.getStartedDate());
        this.expiredDate = DateTimeUtil.formatDateTime(bannedUser.getExpiredDate());
        this.reason = reason;
    }
}
