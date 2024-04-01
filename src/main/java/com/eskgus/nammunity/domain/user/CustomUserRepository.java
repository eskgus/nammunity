package com.eskgus.nammunity.domain.user;

import com.eskgus.nammunity.web.dto.user.UsersListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomUserRepository {
    Page<UsersListDto> searchByNickname(String keywords, Pageable pageable);
}
