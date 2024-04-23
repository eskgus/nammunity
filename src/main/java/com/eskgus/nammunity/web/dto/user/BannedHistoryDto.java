package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.domain.user.BannedUsers;
import com.eskgus.nammunity.util.DateTimeUtil;
import lombok.Getter;

@Getter
public class BannedHistoryDto {
    private final int count;
    private final String period;
    private final String startedDate;
    private final String expiredDate;

    public BannedHistoryDto(BannedUsers bannedUser) {
        this.count = bannedUser.getCount();
        this.period = DateTimeUtil.convertPeriodToString(bannedUser.getPeriod());
        this.startedDate = DateTimeUtil.formatDateTime(bannedUser.getStartedDate());
        this.expiredDate = DateTimeUtil.formatDateTime(bannedUser.getExpiredDate());
    }
}
