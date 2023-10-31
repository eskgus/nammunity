package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.*;
import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class BannedUsersServiceTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BannedUsersService bannedUsersService;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    @Autowired
    private TypesRepository typesRepository;

    @Autowired
    private ContentReportsRepository contentReportsRepository;

    @Autowired
    private ReasonsRepository reasonsRepository;

    @BeforeEach
    public void setup() {
        log.info("setup.....");

        // 1. user 테이블에 user 저장
        User user = User.builder()
                .username("username111").password("password111").nickname("nickname1")
                .email("email111@naver.com").role(Role.USER).build();
        userRepository.save(user);
        Assertions.assertThat(userRepository.count()).isOne();

        // 2. 사용자 신고 * 3
        Types type = typesRepository.findById(3L).get();
        Long[] reasonIdArr = {1L, 2L, 8L};
        List<Reasons> reasons = new ArrayList<>();
        for (Long id : reasonIdArr) {
            reasons.add(reasonsRepository.findById(id).get());
        }
        for (int i = 0; i < 3; i++) {
            Reasons reason = reasons.get(i);
            String otherReason = reason.getDetail().equals("기타") ? "기타 사유" : null;
            ContentReports contentReport = ContentReports.builder()
                    .user(user).reporter(user).types(type).reasons(reason).otherReasons(otherReason).build();
            contentReportsRepository.save(contentReport);
        }
    }

    @Test
    public void banUser() {
        // 1. user 테이블에 user 저장 + 사용자 신고 * 3
        User user = userRepository.findById(1L).get();

        // 2. banned user 생성 및 업데이트
        // 2-1. 누적 정지 횟수: 0 (0일 -> 1주)
        // user id로 bannedUsersService의 banUser() 호출
        Long bannedUserId = bannedUsersService.banUser(user.getId());

        // banUser()가 리턴한 id로 BannedUsers 검색되나 확인
        Optional<BannedUsers> result1 = bannedUsersRepository.findById(bannedUserId);
        Assertions.assertThat(result1).isPresent();
        BannedUsers bannedUser = result1.get();

        // bannedUser의 user, startedDate, expiredDate, period, count 확인
        LocalDateTime startedDate = bannedUser.getStartedDate();
        LocalDateTime expiredDate = bannedUser.getExpiredDate();
        Period period = bannedUser.getPeriod();
        int count1 = bannedUser.getCount();
        Assertions.assertThat(bannedUser.getUser().getId()).isEqualTo(user.getId());
        Assertions.assertThat(expiredDate).isEqualTo(startedDate.plus(period));
        Assertions.assertThat(period).isEqualTo(Period.ofWeeks(1));
        Assertions.assertThat(count1).isOne();

        // 2-2. 누적 정지 횟수: 1 (1주 -> 1개월)
        bannedUserId = bannedUsersService.banUser(user.getId());
        bannedUser = bannedUsersRepository.findById(bannedUserId).get();

        startedDate = bannedUser.getStartedDate();
        expiredDate = bannedUser.getExpiredDate();
        period = bannedUser.getPeriod();
        int count2 = bannedUser.getCount();
        Assertions.assertThat(bannedUser.getUser().getId()).isEqualTo(user.getId());
        Assertions.assertThat(expiredDate).isEqualTo(startedDate.plus(period));
        Assertions.assertThat(period).isEqualTo(Period.ofMonths(1));
        Assertions.assertThat(count2).isGreaterThan(count1);

        // 2-3. 누적 정지 횟수: 2 (1개월 -> 1년)
        bannedUserId = bannedUsersService.banUser(user.getId());
        bannedUser = bannedUsersRepository.findById(bannedUserId).get();

        startedDate = bannedUser.getStartedDate();
        expiredDate = bannedUser.getExpiredDate();
        period = bannedUser.getPeriod();
        int count3 = bannedUser.getCount();
        Assertions.assertThat(bannedUser.getUser().getId()).isEqualTo(user.getId());
        Assertions.assertThat(expiredDate).isEqualTo(startedDate.plus(period));
        Assertions.assertThat(period).isEqualTo(Period.ofYears(1));
        Assertions.assertThat(count3).isGreaterThan(count2);

        // 2-4. 누적 정지 횟수: 3 (1년 -> 영구)
        bannedUserId = bannedUsersService.banUser(user.getId());
        bannedUser = bannedUsersRepository.findById(bannedUserId).get();

        startedDate = bannedUser.getStartedDate();
        expiredDate = bannedUser.getExpiredDate();
        period = bannedUser.getPeriod();
        int count4 = bannedUser.getCount();
        Assertions.assertThat(bannedUser.getUser().getId()).isEqualTo(user.getId());
        Assertions.assertThat(expiredDate).isEqualTo(startedDate.plus(period));
        Assertions.assertThat(period).isEqualTo(Period.ofYears(100));
        Assertions.assertThat(count4).isGreaterThan(count3);
    }
}
