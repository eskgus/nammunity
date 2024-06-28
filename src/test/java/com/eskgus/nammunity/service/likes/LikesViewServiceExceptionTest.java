package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.domain.enums.ExceptionMessages;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.function.BiFunction;

import static com.eskgus.nammunity.domain.enums.ExceptionMessages.UNAUTHORIZED;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.USERNAME_NOT_FOUND;
import static com.eskgus.nammunity.util.ServiceTestUtil.assertIllegalArgumentException;
import static com.eskgus.nammunity.util.ServiceTestUtil.throwIllegalArgumentException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikesViewServiceExceptionTest {
    @Mock
    private LikesService likesService;

    @Mock
    private PrincipalHelper principalHelper;

    @InjectMocks
    private LikesViewService likesViewService;

    private static final int PAGE = 1;

    @Test
    public void listLikesWithAnonymousUser() {
        testListLikesException(null, UNAUTHORIZED);
    }

    @Test
    public void listLikesWithNonExistentUsername() {
        Principal principal = mock(Principal.class);
        testListLikesException(principal, USERNAME_NOT_FOUND);
    }

    private void testListLikesException(Principal principal, ExceptionMessages exceptionMessage) {
        // given
        BiFunction<User, Pageable, Page<LikesListDto>> finder = mock(BiFunction.class);

        throwIllegalArgumentException(principalHelper::getUserFromPrincipal, principal, true, exceptionMessage);

        // when/then
        assertIllegalArgumentException(() -> likesViewService.listLikes(finder, principal, PAGE), exceptionMessage);

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(likesService, never()).findLikesByUser(any(User.class), eq(finder), eq(PAGE), anyInt());
    }
}
