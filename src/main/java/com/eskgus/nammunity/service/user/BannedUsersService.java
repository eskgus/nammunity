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

import static com.eskgus.nammunity.domain.enums.Fields.OTHER;

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

    @Transactional(readOnly = true)
    public boolean isAccountNonBanned(String username) {
        User user = userService.findByUsername(username);

        return findByUser(user).map(bannedUser -> bannedUser.getExpiredDate().isBefore(LocalDateTime.now()))
                .orElse(true);
    }

    @Transactional(readOnly = true)
    public Optional<BannedUsers> findByUser(User user) {
        return bannedUsersRepository.findByUser(user);
    }

    private BannedUsers saveOrUpdateBannedUser(User user) {
        Optional<BannedUsers> result = findByUser(user);

        LocalDateTime startedDate = LocalDateTime.now();
        String reasonDetail = createReasonDetail(user);

        return result.map(bannedUser -> updateBannedUser(bannedUser, startedDate, reasonDetail))
                .orElseGet(() -> saveBannedUser(user, startedDate, reasonDetail));
    }

    private void sendBannedUserEmail(BannedUsers bannedUser, User user) {
        BannedUsersEmailDto emailDto = BannedUsersEmailDto.builder().bannedUser(bannedUser).build();
        String text = emailService.setBannedUserEmailText(emailDto);
        emailService.send(user.getEmail(), text);
    }

    private String createReasonDetail(User user) {
        ContentReportSummary reportSummary = reportSummaryService.findByUser(user);
        String reasonDetail = reportSummary.getReasons().getDetail();
        if (OTHER.getKey().equals(reasonDetail)) {
            reasonDetail += ": " + reportSummary.getOtherReasons();
        }
        return reasonDetail;
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

    private BannedUsers saveBannedUser(User user, LocalDateTime startedDate, String reasonDetail) {
        Period period = Period.ofWeeks(1);
        LocalDateTime expiredDate = startedDate.plus(period);
        BannedUsers bannedUser = BannedUsers.builder()
                .user(user).startedDate(startedDate).expiredDate(expiredDate).period(period).reason(reasonDetail)
                .build();
        return bannedUsersRepository.save(bannedUser);
    }
}
