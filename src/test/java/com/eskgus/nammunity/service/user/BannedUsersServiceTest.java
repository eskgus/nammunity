package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.reports.ContentReportSummary;
import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.user.*;
import com.eskgus.nammunity.service.email.EmailService;
import com.eskgus.nammunity.service.email.dto.BannedUsersEmailDto;
import com.eskgus.nammunity.service.reports.ReportSummaryService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

import static com.eskgus.nammunity.domain.enums.Fields.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BannedUsersServiceTest {
    @Mock
    private BannedUsersRepository bannedUsersRepository;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Mock
    private ReportSummaryService reportSummaryService;

    @InjectMocks
    private BannedUsersService bannedUsersService;

    private static final Long ID = 1L;
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    public void saveBannedUser() {
        giveBannedUserFinder(null);

        BannedUsers bannedUser = giveBannedUser();
        when(bannedUsersRepository.save(any(BannedUsers.class))).thenReturn(bannedUser);

        testBanUser(bannedUser);

        verify(bannedUsersRepository).save(any(BannedUsers.class));
        verify(bannedUser, never()).update(
                any(LocalDateTime.class), any(LocalDateTime.class), any(Period.class), anyString());
    }

    @Test
    public void updateBannedUserPeriodToOneMonth() {
        testUpdateBannedUser(1, Period.ofMonths(1));
    }

    @Test
    public void updateBannedUserPeriodToOneYear() {
        testUpdateBannedUser(2, Period.ofYears(1));
    }

    @Test
    public void updateBannedUserPeriodToPermanent() {
        testUpdateBannedUser(3, Period.ofYears(100));
    }

    @Test
    public void isAccountNeverBanned() {
        testIsAccountNonBanned(null, true);
    }

    @Test
    public void isAccountBanned() {
        BannedUsers bannedUser = giveBannedUser(NOW.plusDays(1));

        testIsAccountNonBanned(bannedUser, false);
    }

    @Test
    public void isAccountUnbanned() {
        BannedUsers bannedUser = giveBannedUser(NOW.minusDays(1));

        testIsAccountNonBanned(bannedUser, true);
    }

    @Test
    public void findBannedUsersByUser() {
        BannedUsers bannedUser = mock(BannedUsers.class);

        testFindBannedUsersByUser(bannedUser, Optional.of(bannedUser));
    }

    @Test
    public void findBannedUsersByUserWithNonExistentBannedUser() {
        testFindBannedUsersByUser(null, Optional.empty());
    }

    private BannedUsers giveBannedUser() {
        BannedUsers bannedUser = mock(BannedUsers.class);
        when(bannedUser.getId()).thenReturn(ID);

        when(bannedUser.getStartedDate()).thenReturn(NOW);
        when(bannedUser.getExpiredDate()).thenReturn(NOW);

        return bannedUser;
    }

    private BannedUsers giveBannedUser(LocalDateTime expiredDate) {
        BannedUsers bannedUser = mock(BannedUsers.class);
        when(bannedUser.getExpiredDate()).thenReturn(expiredDate);

        return bannedUser;
    }

    private ContentReportSummary giveSummary() {
        ContentReportSummary summary = mock(ContentReportSummary.class);
        when(reportSummaryService.findByUser(any(User.class))).thenReturn(summary);

        return summary;
    }

    private Reasons giveReason() {
        Reasons reason = mock(Reasons.class);
        when(reason.getDetail()).thenReturn(OTHER.getKey());

        return reason;
    }

    private void giveBannedUserFinder(BannedUsers bannedUser) {
        ServiceTestUtil.giveContentFinder(bannedUsersRepository::findByUser, User.class, bannedUser);
    }

    private void testUpdateBannedUser(int count, Period period) {
        BannedUsers bannedUser = giveBannedUser();
        giveBannedUserFinder(bannedUser);
        when(bannedUser.getCount()).thenReturn(count);

        testBanUser(bannedUser);

        verify(bannedUsersRepository, never()).save(any(BannedUsers.class));
        verify(bannedUser).update(
                any(LocalDateTime.class), any(LocalDateTime.class), eq(period), anyString());
    }

    private void testBanUser(BannedUsers bannedUser) {
        // given
        User user = ServiceTestUtil.giveUserId(ID, userService::findById);

        ContentReportSummary summary = giveSummary();

        Reasons reason = giveReason();
        when(summary.getReasons()).thenReturn(reason);

        when(bannedUser.getUser()).thenReturn(user);

        String text = "text";
        when(emailService.setBannedUserEmailText(any(BannedUsersEmailDto.class))).thenReturn(text);

        ServiceTestUtil.giveEmail(user, EMAIL.getKey() + "@naver.com");
        doNothing().when(emailService).send(anyString(), anyString());

        // when
        Long result = bannedUsersService.banUser(user.getId());

        // then
        assertEquals(bannedUser.getId(), result);

        verify(userService).findById(eq(user.getId()));
        verify(bannedUsersRepository).findByUser(eq(user));
        verify(reportSummaryService).findByUser(eq(user));
        verify(emailService).setBannedUserEmailText(any(BannedUsersEmailDto.class));
        verify(emailService).send(eq(user.getEmail()), eq(text));
    }

    private void testIsAccountNonBanned(BannedUsers bannedUser, boolean isAccountNonBanned) {
        // given
        User user = ServiceTestUtil.giveUser(userService::findByUsername, String.class);
        ServiceTestUtil.giveUsername(user, USERNAME.getKey());

        giveBannedUserFinder(bannedUser);

        // when
        boolean result = bannedUsersService.isAccountNonBanned(user.getUsername());

        // then
        assertEquals(isAccountNonBanned, result);

        verify(userService).findByUsername(eq(user.getUsername()));
        verify(bannedUsersRepository).findByUser(eq(user));
    }

    private void testFindBannedUsersByUser(BannedUsers bannedUser, Optional<BannedUsers> optionalBannedUser) {
        // given
        giveBannedUserFinder(bannedUser);

        User user = mock(User.class);

        // when
        Optional<BannedUsers> result = bannedUsersService.findByUser(user);

        // then
        assertEquals(optionalBannedUser, result);

        verify(bannedUsersRepository).findByUser(eq(user));
    }
}
