package com.eskgus.nammunity.domain.user;

import com.eskgus.nammunity.web.dto.user.UsersListDto;

import java.util.List;

public interface CustomUserRepository {
    List<UsersListDto> searchByNickname(String keywords);
}
