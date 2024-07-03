package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.util.ServiceTestUtil;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.security.Principal;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikesViewServiceTest {
    @Mock
    private LikesService likesService;

    @Mock
    private PrincipalHelper principalHelper;

    @InjectMocks
    private LikesViewService likesViewService;

    @Test
    public void listLikes() {
        // given
        BiFunction<User, Pageable, Page<LikesListDto>> finder = mock(BiFunction.class);

        Pair<Principal, User> pair = ServiceTestUtil.givePrincipal(principalHelper::getUserFromPrincipal);
        Principal principal = pair.getFirst();
        User user = pair.getSecond();

        int page = 1;

        Page<LikesListDto> likesPage = ServiceTestUtil.createContentsPage();
        when(likesService.findLikesByUser(any(User.class), any(BiFunction.class), anyInt(), anyInt()))
                .thenReturn(likesPage);

        // when
        ContentsPageDto<LikesListDto> result = likesViewService.listLikes(finder, principal, page);

        // then
        assertEquals(likesPage, result.getContents());

        verify(principalHelper).getUserFromPrincipal(principal, true);
        verify(likesService).findLikesByUser(eq(user), eq(finder), eq(page), anyInt());
    }
}
