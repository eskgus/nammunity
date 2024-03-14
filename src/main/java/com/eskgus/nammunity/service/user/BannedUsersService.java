package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.reports.ContentReportSummary;
import com.eskgus.nammunity.domain.user.BannedUsers;
import com.eskgus.nammunity.domain.user.BannedUsersRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.email.EmailService;
import com.eskgus.nammunity.service.email.dto.BannedUsersEmailDto;
import com.eskgus.nammunity.service.reports.ReportSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BannedUsersService {
    private final BannedUsersRepository bannedUsersRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final ReportSummaryService reportSummaryService;

    @Transactional
    public Long banUser(Long userId) {
        User user = userService.findById(userId);

        BannedUsers bannedUser = saveOrUpdateBannedUser(user);
        sendBannedUserEmail(bannedUser, user);

        return bannedUser.getId();
    }

    private BannedUsers saveOrUpdateBannedUser(User user) {
        Optional<BannedUsers> result = bannedUsersRepository.findByUser(user);

        LocalDateTime startedDate = LocalDateTime.now();
        String reasonDetail = createReasonDetail(user);

        if (result.isEmpty()) {
            return saveBannedUser(user, startedDate, reasonDetail);
        } else {
            return updateBannedUser(result.get(), startedDate, reasonDetail);
        }
    }

    private String createReasonDetail(User user) {
        ContentReportSummary reportSummary = reportSummaryService.findByUser(user);
        String reasonDetail = reportSummary.getReasons().getDetail();
        if (reasonDetail.equals("기타")) {
            reasonDetail += ": " + reportSummary.getOtherReasons();
        }
        return reasonDetail;
    }

    private BannedUsers saveBannedUser(User user, LocalDateTime startedDate, String reasonDetail) {
        Period period = Period.ofWeeks(1);
        LocalDateTime expiredDate = startedDate.plus(period);
        BannedUsers bannedUser = BannedUsers.builder()
                .user(user).startedDate(startedDate).expiredDate(expiredDate).period(period).reason(reasonDetail)
                .build();
        return bannedUsersRepository.save(bannedUser);
    }

    private BannedUsers updateBannedUser(BannedUsers bannedUser, LocalDateTime startedDate, String reasonDetail) {
        Period period = switch (bannedUser.getCount()) {
            case 1 -> Period.ofMonths(1);
            case 2 -> Period.ofYears(1);
            default -> Period.ofYears(100);
        };
        LocalDateTime expiredDate = startedDate.plus(period);
        bannedUser.update(startedDate, expiredDate, period, reasonDetail);
        return bannedUser;
    }

    private void sendBannedUserEmail(BannedUsers bannedUser, User user) {
        BannedUsersEmailDto emailDto = BannedUsersEmailDto.builder().bannedUser(bannedUser).build();
        String text = emailService.setEmailText(emailDto);
        emailService.send(user.getEmail(), text);
    }

    @Transactional(readOnly = true)
    public boolean isAccountNonBanned(String username) {
        User user = userService.findByUsername(username);

        // BannedUsers 테이블에 user가 없으면 활동 정지 x
        Optional<BannedUsers> result = bannedUsersRepository.findByUser(user);
        if (result.isEmpty()) {
            return true;
        }

        // BannedUsers 테이블에 있는 user의 활동 정지 종료일이 현재 날짜 이전이면 활동 정지 x, 아니면 활동 정지 o
        BannedUsers bannedUser = result.get();
        return bannedUser.getExpiredDate().isBefore(LocalDateTime.now());
    }
}
