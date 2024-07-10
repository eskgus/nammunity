package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.domain.user.BannedUsers;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BannedHistoryDto {
    private final int count;
    private final String period;
    private final String startedDate;
    private final String expiredDate;

    public BannedHistoryDto(BannedUsers bannedUser) {
        this.count = bannedUser.getCount();
        this.period = DateTimeUtil.convertPeriodToString(bannedUser.getPeriod());
        this.startedDate = formatDateTime(bannedUser.getStartedDate());
        this.expiredDate = formatDateTime(bannedUser.getExpiredDate());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return DateTimeUtil.formatDateTime(dateTime);
    }
}
