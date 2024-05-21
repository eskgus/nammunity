package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.util.TestDB;
import com.eskgus.nammunity.domain.reports.*;
import com.eskgus.nammunity.domain.user.*;
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
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

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
    private ContentReportSummaryRepository reportSummaryRepository;

    @Autowired
    private BannedUsersService bannedUsersService;

    private User user1;

    @BeforeEach
    public void setUp() {
        Long user1Id = testDB.signUp(1L, Role.USER);
        this.user1 = assertOptionalAndGetEntity(userRepository::findById, user1Id);

        Long user2Id = testDB.signUp(2L, Role.ADMIN);
        User user2 = assertOptionalAndGetEntity(userRepository::findById, user2Id);

        Long reportId = testDB.saveUserReports(user1, user2);
        assertOptionalAndGetEntity(contentReportsRepository::findById, reportId);

        Long reportSummaryId = testDB.saveUserReportSummary(user1, user2);
        assertOptionalAndGetEntity(reportSummaryRepository::findById, reportSummaryId);
    }

    private <T> T assertOptionalAndGetEntity(Function<Long, Optional<T>> finder, Long contentId) {
        return testDB.assertOptionalAndGetEntity(finder, contentId);
    }

    @AfterEach
    public void cleanUp() {
        testDB.cleanUp();
    }

    @Test
    public void banUser() {
        // 1. 누적 정지 횟수: 0 (0일 -> 1주)
        callAndAssertBanUser(Period.ofWeeks(1), 1);

        // 2. 누적 정지 횟수: 1 (1주 -> 1개월)
        callAndAssertBanUser(Period.ofMonths(1), 2);

        // 3. 누적 정지 횟수: 2 (1개월 -> 1년)
        callAndAssertBanUser(Period.ofYears(1), 3);

        // 4. 누적 정지 횟수: 3 (1년 -> 영구)
        callAndAssertBanUser(Period.ofYears(100), 4);
    }

    private void callAndAssertBanUser(Period expectedPeriod, int expectedCount) {
        Long bannedUserId = bannedUsersService.banUser(user1.getId());

        assertBannedUser(bannedUserId, expectedPeriod, expectedCount);
    }

    private void assertBannedUser(Long bannedUserId, Period expectedPeriod, int expectedCount) {
        Optional<BannedUsers> result = bannedUsersRepository.findById(bannedUserId);
        assertThat(result).isPresent();
        BannedUsers bannedUser = result.get();

        assertThat(bannedUser.getUser().getId()).isEqualTo(user1.getId());

        LocalDateTime startedDate = bannedUser.getStartedDate();
        LocalDateTime expiredDate = bannedUser.getExpiredDate();
        Period period = bannedUser.getPeriod();
        assertThat(expiredDate).isEqualTo(startedDate.plus(period));

        assertThat(period).isEqualTo(expectedPeriod);
        assertThat(bannedUser.getCount()).isEqualTo(expectedCount);
    }

    @Test
    public void isAccountNonBanned() {
        // 1. BannedUsers 테이블에 user 존재 x -> 활동 정지 x
        callAndAssertIsAccountNonBanned(true);

        // 2. BannedUsers 테이블에 user 존재 o, expiredDate > 현재 날짜 -> 활동 정지 o
        Long bannedUserId = saveBannedUsers();
        callAndAssertIsAccountNonBanned(false);

        // 3. BannedUsers 테이블에 user 존재 o, expiredDate <= 현재 날짜 -> 활동 정지 x
        updateExpiredDate(bannedUserId, LocalDateTime.now());
        callAndAssertIsAccountNonBanned(true);
    }

    private void callAndAssertIsAccountNonBanned(boolean expectedResult) {
        boolean result = bannedUsersService.isAccountNonBanned(user1.getUsername());
        assertThat(result).isEqualTo(expectedResult);
    }

    private Long saveBannedUsers() {
        Long bannedUserId = testDB.saveBannedUsers(user1, Period.ofWeeks(1));
        assertThat(bannedUsersRepository.count()).isEqualTo(bannedUserId);
        return bannedUserId;
    }

    private void updateExpiredDate(Long bannedUserId, LocalDateTime expiredDate) {
        bannedUsersRepository.updateExpiredDate(bannedUserId, expiredDate);
    }
}
