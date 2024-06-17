package com.eskgus.nammunity.service.user;

import com.eskgus.nammunity.domain.user.UserRepository;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void searchByNickname() {
        // given
        Page<UsersListDto> usersPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.searchByNickname(anyString(), any(Pageable.class))).thenReturn(usersPage);

        String keywords = "keyword";
        int page = 1;
        int size = 4;

        // when
        Page<UsersListDto> result = userService.searchByNickname(keywords, page, size);

        // then
        assertEquals(usersPage, result);

        verify(userRepository).searchByNickname(eq(keywords), any(Pageable.class));
    }
}
