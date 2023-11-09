package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.TestDB;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class BannedUsersServiceTest {
    @Autowired
    private TestDB testDB;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContentReportsRepository contentReportsRepository;

    @Autowired
    private BannedUsersRepository bannedUsersRepository;

    @Autowired
    private BannedUsersService bannedUsersService;

    @BeforeEach
    public void setUp() {
        // 1. user1 회원가입 + user2 (관리자) 회원가입
        User user1 = userRepository.findById(testDB.signUp(1L, Role.USER)).get();
        User user2 = userRepository.findById(testDB.signUp(2L, Role.ADMIN)).get();

        // 2. user2가 user1 사용자 신고 * 3
        testDB.saveUserReports(user1, user2);
        Assertions.assertThat(contentReportsRepository.count()).isGreaterThan(2);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void banUser() {
        // 1. user1 회원가입 + user2 (관리자) 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user2가 user1 사용자 신고 * 3
        // 3. banned user 생성 및 업데이트
        Long userId = user1.getId();
        // 3-1. 누적 정지 횟수: 0 (0일 -> 1주)
        callAndAssertBanUser(userId, Period.ofWeeks(1), 1);

        // 3-2. 누적 정지 횟수: 1 (1주 -> 1개월)
        callAndAssertBanUser(userId, Period.ofMonths(1), 2);

        // 3-3. 누적 정지 횟수: 2 (1개월 -> 1년)
        callAndAssertBanUser(userId, Period.ofYears(1), 3);

        // 3-4. 누적 정지 횟수: 3 (1년 -> 영구)
        callAndAssertBanUser(userId, Period.ofYears(100), 4);
    }

    @Test
    public void isAccountNonBanned() {
        // 1. user1 회원가입 + user2 (관리자) 회원가입
        User user1 = userRepository.findById(1L).get();

        // 2. user2가 user1 사용자 신고 * 3
        // 3. BannedUsers 테이블에 user 존재 x => 활동 정지 x
        String username = user1.getUsername();
        callAndAssertIsAccountNonBanned(username, true);

        // 4. BannedUsers 테이블에 user 존재 o, expiredDate > 현재 날짜 => 활동 정지 o
        // user, startedDate, expiredDate, period, reason으로 BannedUser 만들어서 저장
        testDB.saveBannedUsers(user1, Period.ofWeeks(1));
        callAndAssertIsAccountNonBanned(username, false);

        // 5. BannedUsers 테이블에 user 존재 o, expiredDate <= 현재 날짜 => 활동 정지 x
        // expiredDate 업데이트
        bannedUsersService.updateExpiredDate(user1, LocalDateTime.now());
        callAndAssertIsAccountNonBanned(username, true);
    }

    private void callAndAssertBanUser(Long userId, Period expectedPeriod, int expectedCount) {
        // 1. userId로 bannedUsersService의 banUser() 호출
        Long bannedUserId = bannedUsersService.banUser(userId);

        // 2. banUser()가 리턴한 id로 BannedUsers 검색되나 확인
        Optional<BannedUsers> result = bannedUsersRepository.findById(bannedUserId);
        Assertions.assertThat(result).isPresent();
        BannedUsers bannedUser = result.get();

        // 3. bannedUser의 user, expiredDate, period, count 확인
        LocalDateTime startedDate = bannedUser.getStartedDate();
        LocalDateTime expiredDate = bannedUser.getExpiredDate();
        Period period = bannedUser.getPeriod();

        Assertions.assertThat(bannedUser.getUser().getId()).isEqualTo(userId);
        Assertions.assertThat(expiredDate).isEqualTo(startedDate.plus(period));
        Assertions.assertThat(period).isEqualTo(expectedPeriod);
        Assertions.assertThat(bannedUser.getCount()).isEqualTo(expectedCount);
    }

    private void callAndAssertIsAccountNonBanned(String username, boolean expectedResult) {
        boolean result = bannedUsersService.isAccountNonBanned(username);
        Assertions.assertThat(result).isEqualTo(expectedResult);
    }
}
