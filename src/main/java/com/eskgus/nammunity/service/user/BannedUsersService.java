package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.user.BannedUsers;
import com.eskgus.nammunity.domain.user.BannedUsersRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.email.EmailService;
import com.eskgus.nammunity.service.email.dto.BannedUsersEmailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Service
public class BannedUsersService {
    private final BannedUsersRepository bannedUsersRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final ContentReportsRepository contentReportsRepository;

    @Transactional
    public Long banUser(Long userId) {
        User user = userService.findById(userId);
        Optional<BannedUsers> result = bannedUsersRepository.findByUser(user);

        LocalDateTime startedDate = LocalDateTime.now();
        Period period;
        BannedUsers bannedUser;

        // 1. result가 없으면 (누적 정지 횟수 = 0)
        if (result.isEmpty()) {
            period = Period.ofWeeks(1);
            LocalDateTime expiredDate = startedDate.plus(period);
            bannedUser = BannedUsers.builder()
                    .user(user).startedDate(startedDate).expiredDate(expiredDate).period(period).build();
            bannedUsersRepository.save(bannedUser);
        }

        // 2. result가 있으면 (누적 정지 횟수 >= 1)
        else {
            bannedUser = result.get();
            period = switch (bannedUser.getCount()) {
                case 1 -> Period.ofMonths(1);
                case 2 -> Period.ofYears(1);
                default -> Period.ofYears(100);
            };
            LocalDateTime expiredDate = startedDate.plus(period);
            bannedUser.update(startedDate, expiredDate, period);
        }

        // 3. 정지 사유 + 정지 기간 담은 메일 발송
        // reports에서 user로 reason 추출
        Reasons reason = contentReportsRepository.findReasonByUsers(user);
        String reasonDetail = reason.getDetail();
        if (reasonDetail.equals("기타")) {
            reasonDetail += ": " + contentReportsRepository.findOtherReasonByUsers(user, reason);
        }

        // 메일 내용 생성
        BannedUsersEmailDto emailDto = BannedUsersEmailDto.builder()
                .bannedUser(bannedUser).reason(reasonDetail).build();
        log.info("username: " + emailDto.getUsername());
        log.info("period: " + emailDto.getPeriod());
        log.info("startedDate: " + emailDto.getStartedDate());
        log.info("expiredDate: " + emailDto.getExpiredDate());
        log.info("reason: " + emailDto.getReason());

        // 메일 발송
        String text = emailService.setEmailText(emailDto);
        emailService.send(user.getEmail(), text);

        return bannedUser.getId();
    }
}
