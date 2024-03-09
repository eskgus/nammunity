package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.reports.ContentReportsRepository;
import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.user.BannedUsers;
import com.eskgus.nammunity.domain.user.BannedUsersRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.email.EmailService;
import com.eskgus.nammunity.service.email.dto.BannedUsersEmailDto;
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
    private final ContentReportsRepository contentReportsRepository;

    @Transactional
    public Long banUser(Long userId) {
        User user = userService.findById(userId);
        Optional<BannedUsers> result = bannedUsersRepository.findByUser(user);

        // 1. reports에서 user로 reason 추출해서 정지 사유 생성
        Reasons reason = contentReportsRepository.findReasonByContents(user);
        // 신고 되지 않은 사용자면 예외 발생
        if (reason == null) {
            throw new IllegalArgumentException("신고 내역이 없는 회원입니다.");
        }
        String reasonDetail = reason.getDetail();
        if (reasonDetail.equals("기타")) {
            reasonDetail += ": " + contentReportsRepository.findOtherReasonByContents(user, reason);
        }

        LocalDateTime startedDate = LocalDateTime.now();
        Period period;
        BannedUsers bannedUser;

        // 2. result가 없으면 (누적 정지 횟수 = 0)
        if (result.isEmpty()) {
            period = Period.ofWeeks(1);
            LocalDateTime expiredDate = startedDate.plus(period);
            bannedUser = BannedUsers.builder()
                    .user(user).startedDate(startedDate).expiredDate(expiredDate).period(period).reason(reasonDetail)
                    .build();
            bannedUsersRepository.save(bannedUser);
        }

        // 3. result가 있으면 (누적 정지 횟수 >= 1)
        else {
            bannedUser = result.get();
            period = switch (bannedUser.getCount()) {
                case 1 -> Period.ofMonths(1);
                case 2 -> Period.ofYears(1);
                default -> Period.ofYears(100);
            };
            LocalDateTime expiredDate = startedDate.plus(period);
            bannedUser.update(startedDate, expiredDate, period, reasonDetail);
        }

        // 4. 정지 사유 + 정지 기간 담은 메일 발송
        // 메일 내용 생성
        BannedUsersEmailDto emailDto = BannedUsersEmailDto.builder().bannedUser(bannedUser).build();
        // 메일 발송
        String text = emailService.setEmailText(emailDto);
        emailService.send(user.getEmail(), text);

        return bannedUser.getId();
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

    @Transactional
    public void updateExpiredDate(User user, LocalDateTime expiredDate) {
        bannedUsersRepository.updateExpiredDate(user, expiredDate);
    }
}
