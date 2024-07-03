package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.user.BannedUsers;
import com.eskgus.nammunity.domain.user.BannedUsersRepository;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.email.EmailService;
import com.eskgus.nammunity.service.email.dto.BannedUsersEmailDto;
import com.eskgus.nammunity.service.reports.ReportSummaryService;
import com.eskgus.nammunity.util.ServiceTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import java.util.function.Function;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.*;
import static com.eskgus.nammunity.domain.enums.Fields.USERNAME;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BannedUsersServiceExceptionTest {
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

    @Test
    public void banUserWithNonExistentUser() {
        // given
        User user = ServiceTestUtil.giveUserId(ID);

        // when/then
        testBanUserException(userService::findById, USER_NOT_FOUND, user);
    }

    @Test
    public void banUserWithNonExistentUserReportSummary() {
        // given
        User user = ServiceTestUtil.giveUserId(ID, userService::findById);

        BannedUsers bannedUser = mock(BannedUsers.class);
        ServiceTestUtil.giveContentFinder(bannedUsersRepository::findByUser, User.class, bannedUser);

        // when/then
        testBanUserException(reportSummaryService::findByUser, USER_REPORT_SUMMARY_NOT_FOUND, user);
    }

    @Test
    public void isAccountNonBannedWithNonExistentUsername() {
        // given
        String username = USERNAME.getKey();

        ExceptionMessages exceptionMessage = USERNAME_NOT_FOUND;
        throwIllegalArgumentException(userService::findByUsername, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> bannedUsersService.isAccountNonBanned(username), exceptionMessage);

        verify(userService).findByUsername(eq(username));
        verify(bannedUsersRepository, never()).findByUser(any(User.class));
    }

    private <Entity, ParamType> void testBanUserException(Function<ParamType, Entity> finder,
                                                          ExceptionMessages exceptionMessage, User user) {
        throwIllegalArgumentException(finder, exceptionMessage);

        VerificationMode mode = USER_NOT_FOUND.equals(exceptionMessage) ? never() : times(1);

        assertIllegalArgumentException(() -> bannedUsersService.banUser(user.getId()), exceptionMessage);

        verify(userService).findById(eq(user.getId()));
        verify(bannedUsersRepository, mode).findByUser(eq(user));
        verify(reportSummaryService, mode).findByUser(eq(user));
        verify(bannedUsersRepository, never()).save(any(BannedUsers.class));
        verify(emailService, never()).setBannedUserEmailText(any(BannedUsersEmailDto.class));
        verify(emailService, never()).send(anyString(), anyString());
    }

    private <Entity, ParamType> void throwIllegalArgumentException(Function<ParamType, Entity> finder,
                                                                   ExceptionMessages exceptionMessage) {
        ServiceTestUtil.throwIllegalArgumentException(finder, exceptionMessage);
    }

    private void assertIllegalArgumentException(Executable executable, ExceptionMessages exceptionMessage) {
        ServiceTestUtil.assertIllegalArgumentException(executable, exceptionMessage);
    }
}
