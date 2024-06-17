package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.reports.ContentReportSummary;
import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.user.*;
import com.eskgus.nammunity.service.email.EmailService;
import com.eskgus.nammunity.service.email.dto.BannedUsersEmailDto;
import com.eskgus.nammunity.service.reports.ReportSummaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    public void saveBannedUser() {
        Pair<User, BannedUsers> pair = createUserAndBannedUser();

        when(bannedUsersRepository.save(any(BannedUsers.class))).thenReturn(pair.getSecond());

        testBanUser(pair, Optional.empty());

        verify(bannedUsersRepository).save(any(BannedUsers.class));
    }

    @Test
    public void updateBannedUser() {
        Pair<User, BannedUsers> pair = createUserAndMockedBannedUser();

        testBanUser(pair, Optional.of(pair.getSecond()));

        verify(pair.getSecond())
                .update(any(LocalDateTime.class), any(LocalDateTime.class), any(Period.class), anyString());
    }

    @Test
    public void banUserWithNonExistentUserId() {
        // given
        when(userService.findById(anyLong())).thenThrow(IllegalArgumentException.class);

        Long userId = 1L;

        // when/then
        assertThrowsAndVerifyBanUser(userId, never());
    }

    @Test
    public void banUserWithNonExistentReportSummary() {
        // given
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userService.findById(eq(user.getId()))).thenReturn(user);

        when(reportSummaryService.findByUser(any(User.class))).thenThrow(IllegalArgumentException.class);

        // when/then
        assertThrowsAndVerifyBanUser(user.getId(), times(1));
    }

    @Test
    public void isAccountNeverBanned() {
        testIsAccountNonBanned(Optional.empty(), true);
    }

    @Test
    public void isAccountBanned() {
        BannedUsers bannedUser = mock(BannedUsers.class);
        when(bannedUser.getExpiredDate()).thenReturn(LocalDateTime.now().plusDays(1));

        testIsAccountNonBanned(Optional.of(bannedUser), false);
    }

    @Test
    public void isAccountUnbanned() {
        BannedUsers bannedUser = mock(BannedUsers.class);
        when(bannedUser.getExpiredDate()).thenReturn(LocalDateTime.now().minusDays(1));

        testIsAccountNonBanned(Optional.of(bannedUser), true);
    }

    @Test
    public void isAccountNonBannedWithNonExistentUsername() {
        // given
        when(userService.findByUsername(anyString())).thenThrow(IllegalArgumentException.class);

        String username = "username";

        // when/then
        assertThrows(IllegalArgumentException.class, () -> bannedUsersService.isAccountNonBanned(username));

        verify(userService).findByUsername(eq(username));
    }

    private Pair<User, BannedUsers> createUserAndBannedUser() {
        User user = mock(User.class);

        LocalDateTime now = LocalDateTime.now();
        BannedUsers bannedUser = BannedUsers.builder()
                .user(user).startedDate(now).expiredDate(now).period(Period.ofWeeks(1)).build();

        return Pair.of(user, bannedUser);
    }

    private Pair<User, BannedUsers> createUserAndMockedBannedUser() {
        User user = mock(User.class);

        BannedUsers bannedUser = mock(BannedUsers.class);
        LocalDateTime now = LocalDateTime.now();
        when(bannedUser.getId()).thenReturn(1L);
        when(bannedUser.getUser()).thenReturn(user);
        when(bannedUser.getStartedDate()).thenReturn(now);
        when(bannedUser.getExpiredDate()).thenReturn(now);
        when(bannedUser.getCount()).thenReturn(1);

        return Pair.of(user, bannedUser);
    }

    private void testBanUser(Pair<User, BannedUsers> pair, Optional<BannedUsers> optionalBannedUser) {
        // given
        User user = pair.getFirst();
        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn("email@naver.com");
        when(userService.findById(user.getId())).thenReturn(user);

        when(bannedUsersRepository.findByUser(any(User.class))).thenReturn(optionalBannedUser);

        ContentReportSummary reportSummary = mock(ContentReportSummary.class);
        Reasons reason = mock(Reasons.class);
        when(reportSummary.getReasons()).thenReturn(reason);
        when(reason.getDetail()).thenReturn("reason");
        when(reportSummaryService.findByUser(any(User.class))).thenReturn(reportSummary);

        String text = "text";
        when(emailService.setBannedUserEmailText(any(BannedUsersEmailDto.class))).thenReturn(text);

        // when
        Long result = bannedUsersService.banUser(user.getId());

        // then
        assertEquals(pair.getSecond().getId(), result);

        verify(userService).findById(eq(user.getId()));
        verify(bannedUsersRepository).findByUser(eq(user));
        verify(reportSummaryService).findByUser(eq(user));
        verify(emailService).setBannedUserEmailText(any(BannedUsersEmailDto.class));
        verify(emailService).send(eq(user.getEmail()), eq(text));
    }

    private void assertThrowsAndVerifyBanUser(Long userId, VerificationMode mode) {
        assertThrows(IllegalArgumentException.class, () -> bannedUsersService.banUser(userId));

        verify(userService).findById(eq(userId));
        verify(reportSummaryService, mode).findByUser(any(User.class));
        verify(bannedUsersRepository, never()).save(any(BannedUsers.class));
    }

    private void testIsAccountNonBanned(Optional<BannedUsers> optionalBannedUser, boolean isAccountNonBanned) {
        // given
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(userService.findByUsername(user.getUsername())).thenReturn(user);

        when(bannedUsersRepository.findByUser(any(User.class))).thenReturn(optionalBannedUser);

        // when
        boolean result = bannedUsersService.isAccountNonBanned(user.getUsername());

        // then
        assertEquals(isAccountNonBanned, result);

        verify(userService).findByUsername(eq(user.getUsername()));
        verify(bannedUsersRepository).findByUser(eq(user));
    }
}
