package com.eskgus.nammunity.domain.user;

import java.util.List;

public interface CustomUserRepository {
    List<User> searchByNickname(String keywords);
}
